package com.mall.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 分片上传请求DTO
 *
 * @author system
 * @date 2026/09/14
 */
@Data
public class FileChunkUploadDTO {

    /**
     * 文件唯一标识（由初始化接口返回）
     */
    @NotBlank(message = "文件标识不能为空")
    private String fileKey;

    /**
     * 当前分片序号（从1开始）
     */
    @NotNull(message = "分片序号不能为空")
    private Integer chunkIndex;

    /**
     * 分片大小（字节）
     */
    private Long chunkSize;

    /**
     * 分片MD5（用于校验）
     */
    private String chunkMd5;

    /**
     * 是否为最后一个分片
     */
    private Boolean lastChunk = false;
}
