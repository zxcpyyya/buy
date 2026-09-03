package com.mall.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger配置类
 * 
 * @author mall
 * @date 2024/01/01
 */
@Configuration
public class SwaggerConfig {
    
    /**
     * 配置OpenAPI信息
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("商城系统API文档")
                        .version("1.0.0")
                        .description("商城系统后端API接口文档")
                        .contact(new Contact()
                                .name("mall")
                                .email("mall@example.com")));
    }
}
