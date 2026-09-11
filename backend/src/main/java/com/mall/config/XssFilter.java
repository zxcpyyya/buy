package com.mall.config;

import com.mall.common.xss.XssUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * XSS防护过滤器
 * 
 * <p>功能：
 * <ul>
 *   <li>过滤请求参数中的XSS攻击</li>
 *   <li>过滤请求体中的XSS攻击</li>
 *   <li>设置安全响应头</li>
 * </ul>
 *
 * @author xiu
 */
@Slf4j
@Component
@Order(1)
public class XssFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 1. 设置安全响应头
        setSecurityHeaders(httpResponse);

        // 2. 包装请求，过滤XSS
        String contentType = httpRequest.getContentType();
        boolean isJson = contentType != null && contentType.contains("application/json");

        if (isJson) {
            // JSON请求体处理
            chain.doFilter(new XssRequestWrapper(httpRequest), response);
        } else {
            // 表单/URL参数处理
            chain.doFilter(new XssRequestWrapper(httpRequest), response);
        }
    }

    /**
     * 设置安全响应头
     */
    private void setSecurityHeaders(HttpServletResponse response) {
        // 防止XSS
        response.setHeader("X-XSS-Protection", "1; mode=block");
        
        // 防止点击劫持
        response.setHeader("X-Frame-Options", "DENY");
        
        // 防止MIME类型嗅探
        response.setHeader("X-Content-Type-Options", "nosniff");
        
        // 内容安全策略（严格模式）
        response.setHeader("Content-Security-Policy", 
            "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self'; connect-src 'self'");
        
        // Referrer策略
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        
        // 权限策略
        response.setHeader("Permissions-Policy", "geolocation=(), microphone=(), camera=()");
    }

    @Override
    public void init(FilterConfig filterConfig) {
        log.info("XSS防护过滤器初始化");
    }

    @Override
    public void destroy() {
        log.info("XSS防护过滤器销毁");
    }
}
