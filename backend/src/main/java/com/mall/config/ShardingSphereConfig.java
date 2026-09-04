package com.mall.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * ShardingSphere 分库分表配置
 *
 * 切换方式：
 * - 开发模式（单机）：spring.profiles.active=dev
 * - 分库分表模式：spring.profiles.active=sharding
 *
 * 配置说明见 application-sharding.yml
 *
 * @author xiu
 * @date 2026/09/03
 */
@Configuration
@ConditionalOnProperty(name = "spring.shardingsphere.enabled", havingValue = "true")
public class ShardingSphereConfig {
    // ShardingSphere 5.x 在 Spring Boot 下自动装配
    // 所有配置在 application-sharding.yml 中定义
    // 此处可添加自定义 Bean（如分片键生成器、加密算法等）
}
