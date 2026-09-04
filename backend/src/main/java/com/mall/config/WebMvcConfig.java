package com.mall.config;

import com.mall.interceptor.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置类
 * 
 * @author xiu
 * @date 2024/01/01
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    
    /**
     * 认证拦截器
     */
    private final AuthInterceptor authInterceptor;
    
    /**
     * 配置拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
            .addPathPatterns("/api/**")
            .excludePathPatterns(
                "/api/user/login",
                "/api/user/register",
                "/api/product/list",
                "/api/product/detail",
                "/api/product/hot",
                "/api/product/new",
                "/api/category/tree",
                "/swagger-ui/**",
                "/v3/api-docs/**"
            );
    }
}
