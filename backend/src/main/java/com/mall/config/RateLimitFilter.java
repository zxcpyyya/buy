package com.mall.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 请求限流过滤器
 * 
 * <p>功能：
 * <ul>
 *   <li>全局限流</li>
 *   <li>IP限流</li>
 *   <li>接口限流</li>
 *   <li>登录接口特殊限流</li>
 * </ul>
 *
 * @author xiu
 */
@Slf4j
@Component
@Order(2)
public class RateLimitFilter implements Filter {

    /**
     * 请求计数器（滑动窗口）
     */
    private final ConcurrentMap<String, SlidingWindowCounter> counters = new ConcurrentHashMap<>();

    /**
     * 限流配置（默认值，生产环境建议从配置读取）
     */
    private static final int GLOBAL_QPS = 100;
    private static final int IP_QPS = 30;
    private static final int LOGIN_QPS = 5;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 生成请求ID
        String requestId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        httpRequest.setAttribute("requestId", requestId);
        httpResponse.setHeader("X-Request-Id", requestId);

        String uri = httpRequest.getRequestURI();
        String clientIp = getClientIp(httpRequest);

        // 1. 检查全局限流
        if (!checkRateLimit("global", GLOBAL_QPS)) {
            log.warn("全局限流触发: requestId={}, uri={}", requestId, uri);
            writeResponse(httpResponse, 429, "请求过于频繁，请稍后再试");
            return;
        }

        // 2. 检查IP限流
        if (!checkRateLimit("ip:" + clientIp, IP_QPS)) {
            log.warn("IP限流触发: requestId={}, ip={}, uri={}", requestId, clientIp, uri);
            writeResponse(httpResponse, 429, "请求过于频繁，请稍后再试");
            return;
        }

        // 3. 登录接口特殊限流
        if (isLoginEndpoint(uri)) {
            if (!checkRateLimit("login:" + clientIp, LOGIN_QPS)) {
                log.warn("登录限流触发: requestId={}, ip={}", requestId, clientIp);
                writeResponse(httpResponse, 429, "登录请求过于频繁，请稍后再试");
                return;
            }
        }

        // 4. 写入限流信息到响应头
        httpResponse.setHeader("X-RateLimit-Remaining", String.valueOf(
            getRemaining("global", GLOBAL_QPS)));

        chain.doFilter(request, response);

        // 清理过期计数器
        cleanExpiredCounters();
    }

    /**
     * 检查限流
     */
    private boolean checkRateLimit(String key, int qps) {
        SlidingWindowCounter counter = counters.computeIfAbsent(key, k -> new SlidingWindowCounter());
        return counter.tryAcquire(qps);
    }

    /**
     * 获取剩余配额
     */
    private int getRemaining(String key, int qps) {
        SlidingWindowCounter counter = counters.get(key);
        if (counter == null) {
            return qps;
        }
        return Math.max(0, qps - counter.getCount());
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 多级代理时取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }

    /**
     * 判断是否为登录接口
     */
    private boolean isLoginEndpoint(String uri) {
        return uri.contains("/login") || uri.contains("/auth/login");
    }

    /**
     * 写入限流响应
     */
    private void writeResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(String.format(
            "{\"code\":%d,\"message\":\"%s\",\"timestamp\":%d}",
            status, message, System.currentTimeMillis()));
    }

    /**
     * 清理过期计数器
     */
    private void cleanExpiredCounters() {
        if (counters.size() > 10000) {
            counters.entrySet().removeIf(entry -> entry.getValue().isExpired());
        }
    }

    @Override
    public void init(FilterConfig filterConfig) {
        log.info("限流过滤器初始化，全局限流: {}QPS, IP限流: {}QPS, 登录限流: {}QPS",
            GLOBAL_QPS, IP_QPS, LOGIN_QPS);
    }

    @Override
    public void destroy() {
        counters.clear();
        log.info("限流过滤器销毁");
    }

    /**
     * 滑动窗口计数器
     */
    private static class SlidingWindowCounter {
        private final AtomicLong windowStart = new AtomicLong(System.currentTimeMillis());
        private final AtomicLong count = new AtomicLong(0);

        public boolean tryAcquire(int maxQps) {
            long now = System.currentTimeMillis();
            long start = windowStart.get();

            // 滑动窗口：1秒
            if (now - start >= 1000) {
                // 重置窗口
                if (windowStart.compareAndSet(start, now)) {
                    count.set(0);
                }
            }

            // 检查配额
            if (count.incrementAndGet() <= maxQps) {
                return true;
            }

            // 超配额，递减回去
            count.decrementAndGet();
            return false;
        }

        public int getCount() {
            return (int) count.get();
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - windowStart.get() > 60000;
        }
    }
}
