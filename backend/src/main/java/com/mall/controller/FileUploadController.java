package com.mall.controller;

import com.mall.common.annotation.RequireLogin;
import com.mall.common.result.Result;
import com.mall.dto.ChunkUploadDTO;
import com.mall.dto.FileMergeDTO;
import com.mall.dto.FileUploadInitDTO;
import com.mall.service.FileUploadService;
import com.mall.vo.ChunkUploadVO;
import com.mall.vo.FileUploadInitVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件上传Controller
 * 支持普通上传、分片上传和断点续传
 *
 * @author system
 * @date 2026/09/14
 */
@Slf4j
@RestController
@RequestMapping("/api/file")
@Tag(name = "文件上传", description = "文件上传接口，支持普通上传、分片上传和断点续传")
public class FileUploadController {

    @Resource
    private FileUploadService fileUploadService;

    /**
     * 初始化上传
     * <p>
     * 流程：
     * 1. 前端计算文件MD5
     * 2. 前端检查文件大小，决定是否分片
     * 3. 调用此接口初始化上传，获取fileKey和已上传的分片列表
     *
     * @param initDTO 上传初始化参数
     * @return 上传初始化响应，包含fileKey和已上传的分片列表
     */
    @PostMapping("/init")
    @RequireLogin
    @Operation(summary = "初始化上传", description = "初始化文件上传，返回fileKey和已上传的分片列表（用于断点续传）")
    public Result<FileUploadInitVO> initUpload(@Valid @RequestBody FileUploadInitDTO initDTO) {
        Long userId = getUserId();
        log.info("初始化上传请求, fileName={}, fileSize={}, userId={}", 
            initDTO.getFileName(), initDTO.getFileSize(), userId);

        FileUploadInitVO result = fileUploadService.initUpload(initDTO, userId);
        return Result.success(result);
    }

    /**
     * 上传分片
     * <p>
     * 前端需要：
     * 1. 按照分片索引逐一上传每个分片
     * 2. 如果某个分片上传失败，下次从已上传分片列表中排除该分片后重试
     * 3. 所有分片上传完成后，调用合并接口
     *
     * @param fileKey    文件唯一标识
     * @param chunkIndex 分片序号（从1开始）
     * @param chunkMd5  分片MD5
     * @param file       分片文件
     * @return 分片上传结果
     */
    @PostMapping("/chunk")
    @RequireLogin
    @Operation(summary = "上传分片", description = "上传单个分片文件，支持断点续传")
    public Result<ChunkUploadVO> uploadChunk(
            @Parameter(description = "文件唯一标识") 
            @RequestParam String fileKey,
            
            @Parameter(description = "分片序号（从1开始）") 
            @RequestParam Integer chunkIndex,
            
            @Parameter(description = "分片MD5") 
            @RequestParam String chunkMd5,
            
            @Parameter(description = "分片大小（字节）") 
            @RequestParam Long chunkSize,
            
            @Parameter(description = "总分片数") 
            @RequestParam Integer totalChunks,
            
            @Parameter(description = "文件MD5") 
            @RequestParam String fileMd5,
            
            @Parameter(description = "分片文件") 
            @RequestParam("file") MultipartFile file) {

        Long userId = getUserId();
        log.info("上传分片请求, fileKey={}, chunkIndex={}, fileSize={}, userId={}", 
            fileKey, chunkIndex, file.getSize(), userId);

        ChunkUploadDTO chunkDTO = new ChunkUploadDTO();
        chunkDTO.setFileKey(fileKey);
        chunkDTO.setChunkIndex(chunkIndex);
        chunkDTO.setChunkSize(chunkSize);
        chunkDTO.setChunkMd5(chunkMd5);
        chunkDTO.setTotalChunks(totalChunks);
        chunkDTO.setFileMd5(fileMd5);

        ChunkUploadVO result = fileUploadService.uploadChunk(chunkDTO, file, userId);
        return Result.success(result);
    }

    /**
     * 合并分片
     * <p>
     * 所有分片上传完成后调用此接口合并文件
     *
     * @param mergeDTO 合并参数
     * @return 文件访问URL
     */
    @PostMapping("/merge")
    @RequireLogin
    @Operation(summary = "合并分片", description = "所有分片上传完成后调用此接口合并文件")
    public Result<String> mergeChunks(@Valid @RequestBody FileMergeDTO mergeDTO) {
        Long userId = getUserId();
        log.info("合并分片请求, fileKey={}, userId={}", mergeDTO.getFileKey(), userId);

        String fileUrl = fileUploadService.mergeChunks(mergeDTO, userId);
        return Result.success(fileUrl);
    }

    /**
     * 普通文件上传（小于2MB）
     * <p>
     * 小文件直接上传，无需分片
     *
     * @param file 文件
     * @return 文件访问URL
     */
    @PostMapping("/upload")
    @RequireLogin
    @Operation(summary = "普通上传", description = "小于2MB的文件直接上传，无需分片")
    public Result<String> uploadSimple(
            @Parameter(description = "文件") 
            @RequestParam("file") MultipartFile file) {

        Long userId = getUserId();
        log.info("普通上传请求, fileName={}, fileSize={}, userId={}", 
            file.getOriginalFilename(), file.getSize(), userId);

        String fileUrl = fileUploadService.uploadSimple(file, userId);
        return Result.success(fileUrl);
    }

    /**
     * 获取已上传的分片列表
     * <p>
     * 用于断点续传时查询哪些分片已经上传成功
     *
     * @param fileKey 文件唯一标识
     * @return 已上传的分片序号列表
     */
    @GetMapping("/chunks/{fileKey}")
    @RequireLogin
    @Operation(summary = "查询已上传分片", description = "查询某个文件已上传的分片列表，用于断点续传")
    public Result<List<Integer>> getUploadedChunks(
            @Parameter(description = "文件唯一标识") 
            @PathVariable String fileKey) {

        List<Integer> chunks = fileUploadService.getUploadedChunks(fileKey);
        return Result.success(chunks);
    }

    /**
     * 获取当前用户ID
     */
    private Long getUserId() {
        try {
            return com.mall.context.UserContext.getUserId();
        } catch (Exception e) {
            return 0L; // 未登录用户
        }
    }
}
