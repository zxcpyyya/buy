package com.mall;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 商城系统启动类
 * 
 * @author mall
 * @date 2024/01/01
 */
@SpringBootApplication
@MapperScan("com.mall.mapper")
public class MallApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(MallApplication.class, args);
    }
}
