package com.mall.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(name = "ChunkUploadDTO", description = "分片上传请求")
public class ChunkUploadDTO {

    @Schema(description = "文件唯一标识")
    @NotBlank(message = "文件标识不能为空")
    private String fileKey;

    @Schema(description = "分片序号（从1开始）")
    @NotNull(message = "分片序号不能为空")
    private Integer chunkIndex;

    @Schema(description = "分片大小（字节）")
    @NotNull(message = "分片大小不能为空")
    private Long chunkSize;

    @Schema(description = "分片MD5")
    @NotBlank(message = "分片MD5不能为空")
    private String chunkMd5;

    @Schema(description = "总分片数")
    @NotNull(message = "总分片数不能为空")
    private Integer totalChunks;

    @Schema(description = "文件MD5")
    @NotBlank(message = "文件MD5不能为空")
    private String fileMd5;
}
