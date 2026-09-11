package com.mall.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 安全配置属性
 * 
 * @author xiu
 */
@Data
@Component
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

    /**
     * 限流配置
     */
    private RateLimit rateLimit = new RateLimit();

    @Data
    public static class RateLimit {
        /**
         * 是否启用限流
         */
        private boolean enabled = true;

        /**
         * 登录接口限流：每秒令牌数
         */
        private int loginRate = 5;

        /**
         * 登录接口限流：桶容量（突发容量）
         */
        private int loginCapacity = 10;

        /**
         * 全局限流：每秒令牌数
         */
        private int globalRate = 100;

        /**
         * 全局限流：桶容量
         */
        private int globalCapacity = 200;

        /**
         * IP黑名单缓存时间（秒）
         */
        private long blockDuration = 300;

        /**
         * 连续失败登录次数阈值（触发临时封禁）
         */
        private int maxFailAttempts = 5;

        /**
         * 封禁时间（秒）- 连续失败登录后
         */
        private long failBlockDuration = 600;
    }

    /**
     * XSS防护配置
     */
    private Xss xss = new Xss();

    @Data
    public static class Xss {
        /**
         * 是否启用XSS防护
         */
        private boolean enabled = true;

        /**
         * 是否过滤URL参数
         */
        private boolean filterParams = true;

        /**
         * 是否过滤请求体
         */
        private boolean filterBody = true;

        /**
         * 是否过滤请求头
         */
        private boolean filterHeaders = false;

        /**
         * 危险字符替换
         */
        private String replacement = "";
    }

    /**
     * CSRF防护配置
     */
    private Csrf csrf = new Csrf();

    @Data
    public static class Csrf {
        /**
         * 是否启用CSRF防护
         */
        private boolean enabled = false;
    }
}
