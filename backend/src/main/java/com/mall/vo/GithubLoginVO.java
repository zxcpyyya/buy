package com.mall.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * GitHub 登录响应VO
 *
 * @author system
 * @date 2026/09/15
 */
@Data
@Builder
@Schema(name = "GithubLoginVO", description = "GitHub登录响应")
public class GithubLoginVO {

    @Schema(description = "是否是新用户")
    private Boolean isNewUser;

    @Schema(description = "JWT Token")
    private String token;

    @Schema(description = "Token类型")
    private String tokenType;

    @Schema(description = "GitHub用户ID")
    private String githubId;

    @Schema(description = "GitHub用户名")
    private String githubUsername;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "用户头像")
    private String avatar;

    @Schema(description = "用户邮箱")
    private String email;
}
