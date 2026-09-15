package com.mall.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 文件合并请求DTO
 *
 * @author system
 * @date 2026/09/14
 */
@Data
@Schema(name = "FileMergeDTO", description = "文件合并请求")
public class FileMergeDTO {

    @Schema(description = "文件唯一标识")
    @NotBlank(message = "文件标识不能为空")
    private String fileKey;

    @Schema(description = "文件MD5")
    @NotBlank(message = "文件MD5不能为空")
    private String fileMd5;
}
