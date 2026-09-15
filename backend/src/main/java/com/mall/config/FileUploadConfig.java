package com.mall.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件上传资源配置
 * 配置静态资源映射，使上传的文件可以通过URL访问
 *
 * @author system
 * @date 2026/09/14
 */
@Configuration
public class FileUploadConfig implements WebMvcConfigurer {

    @Value("${file.upload.path:/tmp/mall-uploads}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置文件访问路径映射
        // 请求路径: /files/** -> 本地文件路径: /tmp/mall-uploads/**
        String resourcePath = Paths.get(uploadPath).toUri().toString();
        
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + resourcePath);
        
        // 配置Swagger资源
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/");
        
        registry.addResourceHandler("/v3/api-docs/**")
                .addResourceLocations("classpath:/META-INF/resources/");
    }
}
