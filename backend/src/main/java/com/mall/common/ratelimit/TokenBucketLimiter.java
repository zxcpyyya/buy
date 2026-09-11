package com.mall.common.ratelimit;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 令牌桶限流器
 * 纯Java实现，基于滑动窗口算法
 *
 * <p>特性：
 * <ul>
 *   <li>线程安全</li>
 *   <li>支持突发流量（预消费）</li>
 *   <li>平滑限流</li>
 *   <li>无外部依赖</li>
 * </ul>
 *
 * @author xiu
 */
@Slf4j
public class TokenBucketLimiter {

    /**
     * 限流器缓存
     * key: 限流key（如IP、用户ID、接口路径）
     * value: RateLimiter实例
     */
    private static final Map<String, RateLimiterState> LIMITERS = new ConcurrentHashMap<>();

    /**
     * 封禁记录
     * key: IP地址
     * value: 封禁截止时间戳
     */
    private static final Map<String, Long> BLOCKED_IPS = new ConcurrentHashMap<>();

    /**
     * 失败登录记录
     * key: IP地址
     * value: 连续失败次数
     */
    private static final Map<String, FailLoginRecord> FAIL_RECORDS = new ConcurrentHashMap<>();

    /**
     * 失败登录记录
     */
    private record FailLoginRecord(int count, long lastFailTime) {}

    /**
     * 限流器状态
     */
    private static class RateLimiterState {
        private final AtomicLong tokens;
        private final AtomicLong lastRefillTime;
        private final double refillRate;
        private final int capacity;

        RateLimiterState(double permitsPerSecond, int capacity) {
            this.refillRate = permitsPerSecond;
            this.capacity = capacity;
            this.tokens = new AtomicLong(capacity);
            this.lastRefillTime = new AtomicLong(System.currentTimeMillis());
        }

        synchronized boolean tryAcquire() {
            refill();
            if (tokens.get() > 0) {
                tokens.decrementAndGet();
                return true;
            }
            return false;
        }

        private void refill() {
            long now = System.currentTimeMillis();
            long last = lastRefillTime.get();
            long elapsed = now - last;

            if (elapsed >= 100) { // 至少100ms才补充
                // 计算应该补充的令牌数
                double tokensToAdd = (elapsed / 1000.0) * refillRate;
                long newTokens = Math.min(capacity, tokens.get() + (long) tokensToAdd);
                tokens.set(newTokens);
                lastRefillTime.set(now);
            }
        }

        int getAvailableTokens() {
            return (int) tokens.get();
        }
    }

    /**
     * 获取或创建限流器
     *
     * @param key      限流key
     * @param permits  每秒令牌数
     * @param capacity 桶容量（突发容量）
     * @return RateLimiterState
     */
    public static RateLimiterState getOrCreate(String key, double permits, int capacity) {
        return LIMITERS.computeIfAbsent(key, k -> {
            log.debug("创建限流器: key={}, permits={}, capacity={}", key, permits, capacity);
            return new RateLimiterState(permits, capacity);
        });
    }

    /**
     * 尝试获取令牌（不阻塞）
     *
     * @param key     限流key
     * @param permits  每秒令牌数
     * @param capacity 桶容量
     * @return true-获取成功, false-限流拒绝
     */
    public static boolean tryAcquire(String key, double permits, int capacity) {
        // 检查是否被封禁
        if (isBlocked(key)) {
            log.warn("IP被封禁: {}", key);
            return false;
        }

        RateLimiterState limiter = getOrCreate(key, permits, capacity);
        boolean acquired = limiter.tryAcquire();

        if (!acquired) {
            log.warn("限流触发: key={}, permits={}", key, permits);
        }

        return acquired;
    }

    /**
     * 尝试获取令牌（带超时）
     *
     * @param key      限流key
     * @param permits  每秒令牌数
     * @param capacity 桶容量
     * @param timeout  超时时间
     * @param unit     时间单位
     * @return true-获取成功, false-限流拒绝
     */
    public static boolean tryAcquire(String key, double permits, int capacity, long timeout, TimeUnit unit) {
        if (isBlocked(key)) {
            log.warn("IP被封禁: {}", key);
            return false;
        }

        long deadline = System.currentTimeMillis() + unit.toMillis(timeout);
        RateLimiterState limiter = getOrCreate(key, permits, capacity);

        while (System.currentTimeMillis() < deadline) {
            if (limiter.tryAcquire()) {
                return true;
            }
            try {
                Thread.sleep(50); // 短暂休眠
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        return false;
    }

    /**
     * 封禁IP
     *
     * @param ip       IP地址
     * @param duration 封禁时长（秒）
     */
    public static void blockIp(String ip, long duration) {
        BLOCKED_IPS.put(ip, System.currentTimeMillis() + duration * 1000);
        log.warn("IP被封禁: ip={}, duration={}秒", ip, duration);

        // 触发GC回收旧记录（可选优化）
        cleanBlockedIps();
    }

    /**
     * 解封IP
     *
     * @param ip IP地址
     */
    public static void unblockIp(String ip) {
        BLOCKED_IPS.remove(ip);
        log.info("IP解封: ip={}", ip);
    }

    /**
     * 检查IP是否被封禁
     *
     * @param ip IP地址
     * @return true-被封禁, false-正常
     */
    public static boolean isBlocked(String ip) {
        Long blockUntil = BLOCKED_IPS.get(ip);
        if (blockUntil == null) {
            return false;
        }

        if (System.currentTimeMillis() > blockUntil) {
            // 封禁已过期，移除记录
            BLOCKED_IPS.remove(ip);
            return false;
        }

        return true;
    }

    /**
     * 记录登录失败
     *
     * @param ip            IP地址
     * @param maxFailCount  最大失败次数
     * @param blockDuration 封禁时长（秒）
     * @return true-达到封禁阈值, false-未达到
     */
    public static boolean recordFailLogin(String ip, int maxFailCount, long blockDuration) {
        FailLoginRecord record = FAIL_RECORDS.compute(ip, (k, v) -> {
            if (v == null) {
                return new FailLoginRecord(1, System.currentTimeMillis());
            }

            // 如果距离上次失败超过30分钟，重置计数
            if (System.currentTimeMillis() - v.lastFailTime() > 30 * 60 * 1000) {
                return new FailLoginRecord(1, System.currentTimeMillis());
            }

            return new FailLoginRecord(v.count() + 1, System.currentTimeMillis());
        });

        if (record.count() >= maxFailCount) {
            // 达到阈值，封禁IP
            blockIp(ip, blockDuration);
            FAIL_RECORDS.remove(ip);
            return true;
        }

        return false;
    }

    /**
     * 清除失败记录（登录成功后调用）
     *
     * @param ip IP地址
     */
    public static void clearFailRecord(String ip) {
        FAIL_RECORDS.remove(ip);
    }

    /**
     * 获取剩余封禁时间
     *
     * @param ip IP地址
     * @return 剩余秒数，0表示未封禁
     */
    public static long getRemainingBlockTime(String ip) {
        Long blockUntil = BLOCKED_IPS.get(ip);
        if (blockUntil == null) {
            return 0;
        }

        long remaining = (blockUntil - System.currentTimeMillis()) / 1000;
        return Math.max(0, remaining);
    }

    /**
     * 清理过期封禁记录
     */
    private static void cleanBlockedIps() {
        long now = System.currentTimeMillis();
        BLOCKED_IPS.entrySet().removeIf(entry -> entry.getValue() < now);
    }

    /**
     * 获取当前被封禁的IP数量（监控用）
     */
    public static int getBlockedIpCount() {
        cleanBlockedIps();
        return BLOCKED_IPS.size();
    }

    /**
     * 获取当前限流器数量（监控用）
     */
    public static int getLimiterCount() {
        return LIMITERS.size();
    }

    /**
     * 清除所有限流器（谨慎使用）
     */
    public static void clearAll() {
        LIMITERS.clear();
        BLOCKED_IPS.clear();
        FAIL_RECORDS.clear();
        log.info("已清除所有限流器状态");
    }
}
