package com.mall.service.impl;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.exception.BusinessException;
import com.mall.dto.ChunkUploadDTO;
import com.mall.dto.FileMergeDTO;
import com.mall.dto.FileUploadInitDTO;
import com.mall.entity.FileChunkDetailDO;
import com.mall.entity.FileChunkRecordDO;
import com.mall.mapper.FileChunkDetailMapper;
import com.mall.mapper.FileChunkRecordMapper;
import com.mall.service.FileUploadService;
import com.mall.vo.ChunkUploadVO;
import com.mall.vo.FileUploadInitVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 文件上传服务实现类
 * 支持普通上传、分片上传和断点续传
 *
 * @author system
 * @date 2026/09/14
 */
@Slf4j
@Service
public class FileUploadServiceImpl implements FileUploadService {

    /**
     * 默认分片大小：5MB
     */
    private static final long DEFAULT_CHUNK_SIZE = 5 * 1024 * 1024L;

    /**
     * 简单上传大小限制：2MB
     */
    private static final long SIMPLE_UPLOAD_MAX_SIZE = 2 * 1024 * 1024L;

    /**
     * 临时分片文件过期时间：7天
     */
    private static final long CHUNK_EXPIRE_DAYS = 7;

    /**
     * 文件存储根目录
     */
    @Value("${file.upload.path:/tmp/mall-uploads}")
    private String uploadPath;

    /**
     * 文件访问基础URL
     */
    @Value("${file.upload.base-url:http://localhost:8080/files}")
    private String baseUrl;

    @Resource
    private FileChunkRecordMapper recordMapper;

    @Resource
    private FileChunkDetailMapper detailMapper;

    @Resource
    private StringRedisTemplate redisTemplate;

    /**
     * Redis Key 前缀
     */
    private static final String REDIS_KEY_PREFIX = "file:upload:";

    /**
     * 初始化上传
     * <p>
     * 流程：
     * 1. 检查文件是否已上传完成
     * 2. 检查是否有未完成的上传记录（断点续传）
     * 3. 创建新的上传记录
     */
    @Override
    public FileUploadInitVO initUpload(FileUploadInitDTO initDTO, Long userId) {
        String fileMd5 = initDTO.getFileMd5();
        String fileKey = generateFileKey(fileMd5, userId);

        // 1. 检查是否已上传完成
        FileChunkRecordDO completedRecord = recordMapper.selectOne(
            new LambdaQueryWrapper<FileChunkRecordDO>()
                .eq(FileChunkRecordDO::getFileMd5, fileMd5)
                .eq(FileChunkRecordDO::getStatus, 1)
                .eq(FileChunkRecordDO::getUserId, userId)
                .last("LIMIT 1")
        );

        if (Objects.nonNull(completedRecord)) {
            // 文件已上传完成，直接返回
            return FileUploadInitVO.builder()
                .fileKey(fileKey)
                .uploadedChunks(List.of())
                .needChunk(false)
                .fileUrl(completedRecord.getFileUrl())
                .chunkSize(initDTO.getChunkSize())
                .totalChunks(initDTO.getTotalChunks())
                .build();
        }

        // 2. 检查是否存在未完成的上传记录（断点续传）
        FileChunkRecordDO existingRecord = recordMapper.selectOne(
            new LambdaQueryWrapper<FileChunkRecordDO>()
                .eq(FileChunkRecordDO::getFileKey, fileKey)
                .eq(FileChunkRecordDO::getStatus, 0)
                .last("LIMIT 1")
        );

        List<Integer> uploadedChunks = new ArrayList<>();

        if (Objects.nonNull(existingRecord)) {
            // 存在未完成的记录，获取已上传的分片
            List<FileChunkDetailDO> details = detailMapper.selectList(
                new LambdaQueryWrapper<FileChunkDetailDO>()
                    .eq(FileChunkDetailDO::getFileKey, fileKey)
                    .eq(FileChunkDetailDO::getUploaded, 1)
            );
            uploadedChunks = details.stream()
                .map(FileChunkDetailDO::getChunkIndex)
                .sorted()
                .collect(Collectors.toList());

            log.info("断点续传检测到未完成上传, fileKey={}, uploadedChunks={}", 
                fileKey, uploadedChunks);
        } else {
            // 3. 创建新的上传记录
            FileChunkRecordDO record = new FileChunkRecordDO();
            record.setFileKey(fileKey);
            record.setFileName(initDTO.getFileName());
            record.setTotalSize(initDTO.getFileSize());
            record.setChunkSize(initDTO.getChunkSize());
            record.setTotalChunks(initDTO.getTotalChunks());
            record.setUploadedChunks(0);
            record.setFileMd5(fileMd5);
            record.setStatus(0); // 上传中
            record.setUserId(userId);
            record.setCreateTime(LocalDateTime.now());
            record.setUpdateTime(LocalDateTime.now());

            recordMapper.insert(record);

            // 同时在Redis中记录，用于快速查询
            String redisKey = REDIS_KEY_PREFIX + fileKey + ":chunks";
            redisTemplate.opsForSet().add(redisKey, "");
            redisTemplate.expire(redisKey, CHUNK_EXPIRE_DAYS, TimeUnit.DAYS);

            log.info("创建新上传记录, fileKey={}", fileKey);
        }

        // 判断是否需要分片
        boolean needChunk = initDTO.getFileSize() > SIMPLE_UPLOAD_MAX_SIZE;

        return FileUploadInitVO.builder()
            .fileKey(fileKey)
            .uploadedChunks(uploadedChunks)
            .needChunk(needChunk)
            .chunkSize(initDTO.getChunkSize())
            .totalChunks(initDTO.getTotalChunks())
            .build();
    }

    /**
     * 分片上传
     * <p>
     * 核心流程：
     * 1. 校验分片信息
     * 2. 保存分片到临时目录
     * 3. 记录分片上传状态
     * 4. 检查是否全部上传完成
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChunkUploadVO uploadChunk(ChunkUploadDTO chunkDTO, MultipartFile file, Long userId) {
        String fileKey = chunkDTO.getFileKey();
        Integer chunkIndex = chunkDTO.getChunkIndex();

        // 1. 校验上传记录是否存在
        FileChunkRecordDO record = recordMapper.selectOne(
            new LambdaQueryWrapper<FileChunkRecordDO>()
                .eq(FileChunkRecordDO::getFileKey, fileKey)
                .last("LIMIT 1")
        );

        if (Objects.isNull(record)) {
            throw new BusinessException("F0001", "上传记录不存在，请先初始化上传");
        }

        if (record.getStatus() == 1) {
            throw new BusinessException("F0002", "文件已上传完成");
        }

        // 2. 检查分片是否已上传（断点续传场景）
        FileChunkDetailDO existingChunk = detailMapper.selectOne(
            new LambdaQueryWrapper<FileChunkDetailDO>()
                .eq(FileChunkDetailDO::getFileKey, fileKey)
                .eq(FileChunkDetailDO::getChunkIndex, chunkIndex)
                .last("LIMIT 1")
        );

        if (Objects.nonNull(existingChunk) && existingChunk.getUploaded() == 1) {
            // 分片已上传，直接返回成功
            log.info("分片已存在，跳过, fileKey={}, chunkIndex={}", fileKey, chunkIndex);

            // 更新已上传分片数
            Long uploadedCount = detailMapper.selectCount(
                new LambdaQueryWrapper<FileChunkDetailDO>()
                    .eq(FileChunkDetailDO::getFileKey, fileKey)
                    .eq(FileChunkDetailDO::getUploaded, 1)
            );

            return ChunkUploadVO.builder()
                .fileKey(fileKey)
                .chunkIndex(chunkIndex)
                .uploadedChunks(uploadedCount.intValue())
                .totalChunks(chunkDTO.getTotalChunks())
                .completed(false)
                .build();
        }

        // 3. 保存分片文件到临时目录
        String chunkDir = getChunkDir(fileKey);
        String chunkFileName = String.format("chunk_%05d", chunkIndex);
        String chunkPath = chunkDir + File.separator + chunkFileName;

        try {
            // 确保目录存在
            File dir = new File(chunkDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 保存分片文件
            file.transferTo(new File(chunkPath));

            // 4. 记录分片上传状态
            if (Objects.isNull(existingChunk)) {
                // 新增分片记录
                FileChunkDetailDO detail = new FileChunkDetailDO();
                detail.setFileKey(fileKey);
                detail.setChunkIndex(chunkIndex);
                detail.setChunkSize(chunkDTO.getChunkSize());
                detail.setChunkPath(chunkPath);
                detail.setChunkMd5(chunkDTO.getChunkMd5());
                detail.setUploaded(1);
                detail.setUploadTime(LocalDateTime.now());
                detail.setCreateTime(LocalDateTime.now());
                detailMapper.insert(detail);
            } else {
                // 更新分片记录
                existingChunk.setUploaded(1);
                existingChunk.setChunkPath(chunkPath);
                existingChunk.setChunkMd5(chunkDTO.getChunkMd5());
                existingChunk.setUploadTime(LocalDateTime.now());
                detailMapper.updateById(existingChunk);
            }

            // 5. 更新上传记录
            Long uploadedCount = detailMapper.selectCount(
                new LambdaQueryWrapper<FileChunkDetailDO>()
                    .eq(FileChunkDetailDO::getFileKey, fileKey)
                    .eq(FileChunkDetailDO::getUploaded, 1)
            );

            record.setUploadedChunks(uploadedCount.intValue());
            record.setUpdateTime(LocalDateTime.now());
            recordMapper.updateById(record);

            // 6. 更新Redis记录
            String redisKey = REDIS_KEY_PREFIX + fileKey + ":chunks";
            redisTemplate.opsForSet().add(redisKey, String.valueOf(chunkIndex));

            // 7. 检查是否全部上传完成
            boolean completed = uploadedCount >= chunkDTO.getTotalChunks();

            log.info("分片上传成功, fileKey={}, chunkIndex={}, progress={}/{}", 
                fileKey, chunkIndex, uploadedCount, chunkDTO.getTotalChunks());

            return ChunkUploadVO.builder()
                .fileKey(fileKey)
                .chunkIndex(chunkIndex)
                .uploadedChunks(uploadedCount.intValue())
                .totalChunks(chunkDTO.getTotalChunks())
                .completed(completed)
                .build();

        } catch (IOException e) {
            log.error("分片保存失败, fileKey={}, chunkIndex={}", fileKey, chunkIndex, e);
            throw new BusinessException("F0003", "分片保存失败");
        }
    }

    /**
     * 合并分片
     * <p>
     * 流程：
     * 1. 校验所有分片是否已上传
     * 2. 按顺序合并所有分片
     * 3. 校验合并后文件的MD5
     * 4. 清理分片临时文件
     * 5. 更新上传记录状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String mergeChunks(FileMergeDTO mergeDTO, Long userId) {
        String fileKey = mergeDTO.getFileKey();
        String fileMd5 = mergeDTO.getFileMd5();

        // 1. 获取上传记录
        FileChunkRecordDO record = recordMapper.selectOne(
            new LambdaQueryWrapper<FileChunkRecordDO>()
                .eq(FileChunkRecordDO::getFileKey, fileKey)
                .last("LIMIT 1")
        );

        if (Objects.isNull(record)) {
            throw new BusinessException("F0001", "上传记录不存在");
        }

        if (record.getStatus() == 1) {
            // 已合并过，直接返回URL
            return record.getFileUrl();
        }

        // 2. 检查所有分片是否已上传
        List<FileChunkDetailDO> chunks = detailMapper.selectList(
            new LambdaQueryWrapper<FileChunkDetailDO>()
                .eq(FileChunkDetailDO::getFileKey, fileKey)
                .eq(FileChunkDetailDO::getUploaded, 1)
                .orderByAsc(FileChunkDetailDO::getChunkIndex)
        );

        if (chunks.size() < record.getTotalChunks()) {
            throw new BusinessException("F0004", 
                "分片未全部上传，已上传 " + chunks.size() + "/" + record.getTotalChunks());
        }

        // 3. 生成最终文件路径
        String finalDir = getFinalFileDir(record.getFileName());
        String finalFileName = generateFinalFileName(record.getFileName(), fileMd5);
        String finalPath = finalDir + File.separator + finalFileName;

        try {
            // 4. 合并分片
            mergeChunksToFile(chunks, finalPath);

            // 5. 校验MD5
            String actualMd5 = calculateFileMd5(finalPath);
            if (!actualMd5.equalsIgnoreCase(fileMd5)) {
                // MD5校验失败，删除合并的文件
                Files.deleteIfExists(Paths.get(finalPath));
                throw new BusinessException("F0005", "文件MD5校验失败");
            }

            // 6. 清理分片临时文件
            String chunkDir = getChunkDir(fileKey);
            FileUtil.del(chunkDir);
            detailMapper.delete(
                new LambdaQueryWrapper<FileChunkDetailDO>()
                    .eq(FileChunkDetailDO::getFileKey, fileKey)
            );

            // 7. 更新上传记录
            String fileUrl = baseUrl + "/" + record.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) 
                + "/" + finalFileName;

            record.setFilePath(finalPath);
            record.setFileUrl(fileUrl);
            record.setStatus(1); // 上传完成
            record.setUpdateTime(LocalDateTime.now());
            recordMapper.updateById(record);

            // 8. 清理Redis记录
            String redisKey = REDIS_KEY_PREFIX + fileKey + ":chunks";
            redisTemplate.delete(redisKey);

            log.info("文件合并完成, fileKey={}, fileUrl={}", fileKey, fileUrl);

            return fileUrl;

        } catch (IOException e) {
            log.error("文件合并失败, fileKey={}", fileKey, e);
            throw new BusinessException("F0003", "文件合并失败");
        }
    }

    /**
     * 普通文件上传（小于2MB）
     */
    @Override
    public String uploadSimple(MultipartFile file, Long userId) {
        if (file.getSize() > SIMPLE_UPLOAD_MAX_SIZE) {
            throw new BusinessException("F0006", "文件超过2MB，请使用分片上传");
        }

        String fileName = file.getOriginalFilename();
        String fileMd5 = calculateMd5(file);
        String fileKey = generateFileKey(fileMd5, userId);

        // 检查是否已上传
        FileChunkRecordDO existing = recordMapper.selectOne(
            new LambdaQueryWrapper<FileChunkRecordDO>()
                .eq(FileChunkRecordDO::getFileMd5, fileMd5)
                .eq(FileChunkRecordDO::getUserId, userId)
                .eq(FileChunkRecordDO::getStatus, 1)
                .last("LIMIT 1")
        );

        if (Objects.nonNull(existing)) {
            return existing.getFileUrl();
        }

        // 保存文件
        String finalDir = getFinalFileDir(fileName);
        String finalFileName = generateFinalFileName(fileName, fileMd5);
        String finalPath = finalDir + File.separator + finalFileName;

        try {
            File dir = new File(finalDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            file.transferTo(new File(finalPath));

            String fileUrl = baseUrl + "/" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) 
                + "/" + finalFileName;

            // 记录
            FileChunkRecordDO record = new FileChunkRecordDO();
            record.setFileKey(fileKey);
            record.setFileName(fileName);
            record.setTotalSize(file.getSize());
            record.setChunkSize(file.getSize());
            record.setTotalChunks(1);
            record.setUploadedChunks(1);
            record.setFileMd5(fileMd5);
            record.setFilePath(finalPath);
            record.setFileUrl(fileUrl);
            record.setStatus(1);
            record.setUserId(userId);
            record.setCreateTime(LocalDateTime.now());
            record.setUpdateTime(LocalDateTime.now());
            recordMapper.insert(record);

            return fileUrl;

        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException("F0003", "文件上传失败");
        }
    }

    /**
     * 查询已上传的分片
     */
    @Override
    public List<Integer> getUploadedChunks(String fileKey) {
        // 先从Redis获取
        String redisKey = REDIS_KEY_PREFIX + fileKey + ":chunks";
        String chunksStr = redisTemplate.opsForSet().members(redisKey).toString();

        if (chunksStr != null && !chunksStr.isEmpty()) {
            return redisTemplate.opsForSet().members(redisKey).stream()
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .sorted()
                .collect(Collectors.toList());
        }

        // 从数据库获取
        List<FileChunkDetailDO> details = detailMapper.selectList(
            new LambdaQueryWrapper<FileChunkDetailDO>()
                .eq(FileChunkDetailDO::getFileKey, fileKey)
                .eq(FileChunkDetailDO::getUploaded, 1)
        );

        return details.stream()
            .map(FileChunkDetailDO::getChunkIndex)
            .sorted()
            .collect(Collectors.toList());
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 生成文件唯一标识
     */
    private String generateFileKey(String fileMd5, Long userId) {
        return userId + "_" + fileMd5;
    }

    /**
     * 获取分片临时目录
     */
    private String getChunkDir(String fileKey) {
        return uploadPath + File.separator + "chunks" + File.separator + fileKey;
    }

    /**
     * 获取最终文件目录
     */
    private String getFinalFileDir(String fileName) {
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String ext = FileUtil.extName(fileName);
        String typeDir = getFileTypeDir(ext);
        return uploadPath + File.separator + typeDir + File.separator + datePath;
    }

    /**
     * 根据文件扩展名获取类型目录
     */
    private String getFileTypeDir(String ext) {
        if (ext == null) {
            return "other";
        }
        ext = ext.toLowerCase();
        if (List.of("jpg", "jpeg", "png", "gif", "bmp", "webp").contains(ext)) {
            return "images";
        } else if (List.of("mp4", "avi", "mov", "wmv", "flv").contains(ext)) {
            return "videos";
        } else if (List.of("mp3", "wav", "flac", "aac", "ogg").contains(ext)) {
            return "audios";
        } else if (List.of("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt").contains(ext)) {
            return "documents";
        } else {
            return "other";
        }
    }

    /**
     * 生成最终文件名
     */
    private String generateFinalFileName(String originalName, String md5) {
        String ext = FileUtil.extName(originalName);
        return md5.substring(0, 16) + "_" + System.currentTimeMillis() 
            + (ext.isEmpty() ? "" : "." + ext);
    }

    /**
     * 合并分片到文件
     */
    private void mergeChunksToFile(List<FileChunkDetailDO> chunks, String finalPath) throws IOException {
        File dir = new File(FileUtil.getParent(finalPath, 1));
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try (FileOutputStream fos = new FileOutputStream(finalPath);
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {

            for (FileChunkDetailDO chunk : chunks) {
                File chunkFile = new File(chunk.getChunkPath());
                if (!chunkFile.exists()) {
                    throw new BusinessException("F0007", "分片文件丢失: " + chunk.getChunkIndex());
                }

                try (FileInputStream fis = new FileInputStream(chunkFile);
                     BufferedInputStream bis = new BufferedInputStream(fis)) {

                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = bis.read(buffer)) != -1) {
                        bos.write(buffer, 0, bytesRead);
                    }
                }
            }

            bos.flush();
        }

        log.info("分片合并完成, chunks={}, finalPath={}", chunks.size(), finalPath);
    }

    /**
     * 计算文件MD5
     */
    private String calculateFileMd5(String filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            return calculateMd5(fis);
        }
    }

    /**
     * 计算MultipartFile的MD5
     */
    private String calculateMd5(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            return calculateMd5(is);
        } catch (IOException e) {
            throw new BusinessException("F0003", "计算文件MD5失败");
        }
    }

    /**
     * 计算输入流的MD5
     */
    private String calculateMd5(InputStream is) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                md.update(buffer, 0, bytesRead);
            }
            byte[] digest = md.digest();

            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }
}
