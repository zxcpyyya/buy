package com.mall.config;

import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Set;

/**
 * 公开接口白名单配置
 * 定义哪些接口不需要登录即可访问
 *
 * @author mall
 */
@Configuration
public class PublicApiConfig {

    /**
     * 公开接口路径（Ant风格匹配）
     */
    public static final Set<String> PUBLIC_PATHS = new HashSet<>();

    static {
        // 首页相关 - 公开
        PUBLIC_PATHS.add("/api/product/list");
        PUBLIC_PATHS.add("/api/product/detail/**");
        PUBLIC_PATHS.add("/api/product/search");
        PUBLIC_PATHS.add("/api/product/hot");
        PUBLIC_PATHS.add("/api/product/recommend");
        PUBLIC_PATHS.add("/api/product/new");

        // 分类相关 - 公开
        PUBLIC_PATHS.add("/api/category/**");
        PUBLIC_PATHS.add("/api/category/list");

        // 营销活动 - 公开
        PUBLIC_PATHS.add("/api/activity/**");
        PUBLIC_PATHS.add("/api/banner/**");

        // 验证码相关 - 公开
        PUBLIC_PATHS.add("/api/captcha/**");
        PUBLIC_PATHS.add("/api/sms/**");

        // 公开文档
        PUBLIC_PATHS.add("/swagger-ui/**");
        PUBLIC_PATHS.add("/v3/api-docs/**");
        PUBLIC_PATHS.add("/doc.html");
        PUBLIC_PATHS.add("/webjars/**");

        // 健康检查
        PUBLIC_PATHS.add("/api/health");
        PUBLIC_PATHS.add("/actuator/**");
    }

    /**
     * 需要强制登录的接口（即使有Token也会验证）
     */
    public static final Set<String> FORCE_LOGIN_PATHS = new HashSet<>();

    static {
        // 下单相关
        FORCE_LOGIN_PATHS.add("/api/order/create");
        FORCE_LOGIN_PATHS.add("/api/order/**/pay");
        FORCE_LOGIN_PATHS.add("/api/order/**/cancel");

        // 购物车相关
        FORCE_LOGIN_PATHS.add("/api/cart/**");

        // 优惠券领取
        FORCE_LOGIN_PATHS.add("/api/coupon/receive");

        // 积分相关
        FORCE_LOGIN_PATHS.add("/api/points/**");
    }

    /**
     * 检查路径是否在白名单中
     */
    public static boolean isPublicPath(String path) {
        for (String pattern : PUBLIC_PATHS) {
            if (matchPath(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查路径是否需要强制登录
     */
    public static boolean isForceLoginPath(String path) {
        for (String pattern : FORCE_LOGIN_PATHS) {
            if (matchPath(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Ant风格路径匹配
     */
    private static boolean matchPath(String pattern, String path) {
        if ("**".equals(pattern)) {
            return true;
        }

        if (pattern.endsWith("/**")) {
            String prefix = pattern.substring(0, pattern.length() - 2);
            return path.startsWith(prefix);
        }

        if (pattern.endsWith("*")) {
            String prefix = pattern.substring(0, pattern.length() - 1);
            return path.startsWith(prefix);
        }

        // 处理 /**/xxx 模式
        if (pattern.contains("/**/")) {
            String[] parts = pattern.split("/\\*\\*/");
            boolean matched = true;
            for (String part : parts) {
                if (!part.isEmpty() && !path.contains(part)) {
                    matched = false;
                    break;
                }
            }
            if (matched) {
                return true;
            }
        }

        return pattern.equals(path);
    }
}
