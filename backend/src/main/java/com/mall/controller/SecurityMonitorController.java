package com.mall.controller;

import com.mall.common.ratelimit.TokenBucketLimiter;
import com.mall.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;

/**
 * 安全监控Controller
 * 
 * <p>提供安全状态监控接口
 *
 * @author xiu
 */
@RestController
@RequestMapping("/api/admin/security")
@RequiredArgsConstructor
@Tag(name = "安全监控", description = "安全状态监控接口")
public class SecurityMonitorController {

    /**
     * 获取安全状态
     */
    @GetMapping("/status")
    @Operation(summary = "安全状态", description = "获取当前安全防护状态")
    public Result<Map<String, Object>> getSecurityStatus() {
        Map<String, Object> status = new HashMap<>();

        // 限流状态
        Map<String, Object> rateLimitStatus = new HashMap<>();
        rateLimitStatus.put("blockedIpCount", TokenBucketLimiter.getBlockedIpCount());
        rateLimitStatus.put("activeLimiters", TokenBucketLimiter.getLimiterCount());
        status.put("rateLimit", rateLimitStatus);

        // JVM状态
        Map<String, Object> jvmStatus = new HashMap<>();
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        jvmStatus.put("heapUsed", memoryMXBean.getHeapMemoryUsage().getUsed());
        jvmStatus.put("heapMax", memoryMXBean.getHeapMemoryUsage().getMax());
        jvmStatus.put("heapUsage", String.format("%.2f%%",
                (double) memoryMXBean.getHeapMemoryUsage().getUsed() /
                memoryMXBean.getHeapMemoryUsage().getMax() * 100));

        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        jvmStatus.put("threadCount", threadMXBean.getThreadCount());
        jvmStatus.put("peakThreadCount", threadMXBean.getPeakThreadCount());
        status.put("jvm", jvmStatus);

        return Result.success(status);
    }

    /**
     * 解封指定IP（仅超管可用）
     */
    @GetMapping("/unblock")
    @Operation(summary = "解封IP", description = "手动解封指定IP地址")
    public Result<Boolean> unblockIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return Result.error("A0401", "IP地址不能为空");
        }

        if (TokenBucketLimiter.isBlocked(ip)) {
            TokenBucketLimiter.unblockIp(ip);
            return Result.success("IP已解封", true);
        }

        return Result.success("IP未被封禁", false);
    }

    /**
     * 获取被封禁的IP列表（仅超管可用）
     */
    @GetMapping("/blocked-ips")
    @Operation(summary = "被封禁IP列表", description = "获取当前被封禁的IP地址列表")
    public Result<Map<String, Object>> getBlockedIps() {
        Map<String, Object> result = new HashMap<>();
        result.put("count", TokenBucketLimiter.getBlockedIpCount());
        // 注意：实际生产环境应返回IP列表和剩余封禁时间
        return Result.success(result);
    }
}
