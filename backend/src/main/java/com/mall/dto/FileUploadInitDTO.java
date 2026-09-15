package com.mall.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 文件上传初始化请求DTO
 *
 * @author system
 * @date 2026/09/14
 */
@Data
@Schema(name = "FileUploadInitDTO", description = "文件上传初始化请求")
public class FileUploadInitDTO {

    @Schema(description = "文件名")
    @NotBlank(message = "文件名不能为空")
    private String fileName;

    @Schema(description = "文件大小（字节）")
    @NotNull(message = "文件大小不能为空")
    @Positive(message = "文件大小必须为正数")
    private Long fileSize;

    @Schema(description = "文件MD5（完整性校验）")
    @NotBlank(message = "文件MD5不能为空")
    private String fileMd5;

    @Schema(description = "分片大小（字节），默认5MB")
    private Long chunkSize = 5 * 1024 * 1024L;

    @Schema(description = "总分片数")
    @NotNull(message = "总分片数不能为空")
    @Positive(message = "总分片数必须为正数")
    private Integer totalChunks;
}
