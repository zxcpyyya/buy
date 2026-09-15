package com.mall.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * GitHub 登录回调DTO
 *
 * @author system
 * @date 2026/09/15
 */
@Data
@Schema(name = "GithubCallbackDTO", description = "GitHub登录回调请求")
public class GithubCallbackDTO {

    @Schema(description = "GitHub授权码")
    @NotBlank(message = "授权码不能为空")
    private String code;

    @Schema(description = "state参数，用于防止CSRF攻击")
    @NotBlank(message = "state不能为空")
    private String state;
}
