package com.mall.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * GitHub OAuth 配置
 *
 * @author system
 * @date 2026/09/15
 */
@Data
@Component
@ConfigurationProperties(prefix = "social.github")
public class GithubProperties {

    /**
     * 客户端 ID
     */
    private String clientId;

    /**
     * 客户端密钥
     */
    private String clientSecret;

    /**
     * 回调地址
     */
    private String redirectUri;

    /**
     * 授权作用域
     */
    private String scope = "read:user,user:email";

    /**
     * GitHub 授权地址
     */
    private String authorizeUrl = "https://github.com/login/oauth/authorize";

    /**
     * GitHub 获取 Token 地址
     */
    private String tokenUrl = "https://github.com/login/oauth/access_token";

    /**
     * GitHub 获取用户信息地址
     */
    private String userInfoUrl = "https://api.github.com/user";

    /**
     * GitHub 获取用户邮箱地址
     */
    private String userEmailsUrl = "https://api.github.com/user/emails";
}
