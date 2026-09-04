package com.mall.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 定时任务配置类
 * 
 * 
 * 1. 使用ScheduledExecutorService代替Timer（多线程并行处理定时任务）
 * 2. 异常处理完善
 * 
 * @author xiu
 * @date 2026/09/03
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
    // 通过@EnableScheduling开启定时任务功能
    // Spring会自动创建一个ScheduledExecutorService来执行定时任务
}
