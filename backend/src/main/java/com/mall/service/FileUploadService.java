package com.mall.service;

import com.mall.dto.ChunkUploadDTO;
import com.mall.dto.FileMergeDTO;
import com.mall.dto.FileUploadInitDTO;
import com.mall.vo.ChunkUploadVO;
import com.mall.vo.FileUploadInitVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传服务接口
 * 支持普通上传、分片上传和断点续传
 *
 * @author system
 * @date 2026/09/14
 */
public interface FileUploadService {

    /**
     * 文件上传初始化
     * 判断是否需要分片，返回已上传的分片列表（断点续传）
     *
     * @param initDTO 上传初始化参数
     * @param userId  用户ID
     * @return 上传初始化响应
     */
    FileUploadInitVO initUpload(FileUploadInitDTO initDTO, Long userId);

    /**
     * 分片上传
     *
     * @param chunkDTO  分片信息
     * @param file      分片文件
     * @param userId    用户ID
     * @return 分片上传结果
     */
    ChunkUploadVO uploadChunk(ChunkUploadDTO chunkDTO, MultipartFile file, Long userId);

    /**
     * 合并分片
     *
     * @param mergeDTO 合并参数
     * @param userId   用户ID
     * @return 文件访问URL
     */
    String mergeChunks(FileMergeDTO mergeDTO, Long userId);

    /**
     * 普通文件上传（小于2MB）
     *
     * @param file   文件
     * @param userId 用户ID
     * @return 文件访问URL
     */
    String uploadSimple(MultipartFile file, Long userId);

    /**
     * 查询已上传的分片
     *
     * @param fileKey 文件唯一标识
     * @return 已上传的分片序号列表
     */
    java.util.List<Integer> getUploadedChunks(String fileKey);
}
