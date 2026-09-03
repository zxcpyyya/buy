package com.mall.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.common.exception.BusinessException;
import com.mall.common.result.Result;
import com.mall.context.UserContext;
import com.mall.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

/**
 * 用户认证拦截器
 * 
 * 遵循阿里Java开发规范：
 * 1. 使用SLF4J日志框架
 * 2. 异常处理完善
 * 3. JWT Token验证
 * 
 * @author mall
 * @date 2024/01/01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {
    
    /**
     * JWT工具类
     */
    private final JwtUtil jwtUtil;
    
    /**
     * ObjectMapper
     */
    private final ObjectMapper objectMapper;
    
    /**
     * 请求头名称
     */
    private static final String AUTHORIZATION_HEADER = "Authorization";
    
    /**
     * Bearer前缀
     */
    private static final String BEARER_PREFIX = "Bearer ";
    
    /**
     * 不需要认证的路径
     */
    private static final String[] EXCLUDE_PATHS = {
        "/api/user/login",
        "/api/user/register",
        "/api/product/list",
        "/api/product/detail",
        "/api/product/hot",
        "/api/product/new",
        "/api/category/tree",
        "/swagger-ui",
        "/v3/api-docs",
        "/error"
    };
    
    /**
     * 前置处理
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取请求路径
        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = requestURI.substring(contextPath.length());
        
        // 检查是否需要认证
        if (this.isExcludePath(path)) {
            return true;
        }
        
        // 获取Token
        String token = this.extractToken(request);
        if (!StringUtils.hasText(token)) {
            this.writeErrorResponse(response, new BusinessException("A0301", "请先登录"));
            return false;
        }
        
        // 验证Token
        if (!jwtUtil.validateToken(token)) {
            this.writeErrorResponse(response, new BusinessException("A0301", "Token已过期，请重新登录"));
            return false;
        }
        
        // 解析Token获取用户信息
        Long userId = jwtUtil.getUserIdFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);
        
        if (Objects.isNull(userId) || !StringUtils.hasText(username)) {
            this.writeErrorResponse(response, new BusinessException("A0301", "无效的Token"));
            return false;
        }
        
        // 设置用户上下文（阿里规范：ThreadLocal使用后必须清理）
        UserContext.UserInfo userInfo = new UserContext.UserInfo();
        userInfo.setUserId(userId);
        userInfo.setUsername(username);
        userInfo.setToken(token);
        UserContext.setUserInfo(userInfo);
        
        log.debug("用户认证成功, userId={}, username={}", userId, username);
        
        return true;
    }
    
    /**
     * 请求完成后清理
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 清理ThreadLocal（阿里规范：ThreadLocal必须清理）
        UserContext.clear();
    }
    
    /**
     * 从请求中提取Token
     *
     * @param request HttpServletRequest
     * @return Token
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
    
    /**
     * 检查是否是排除路径
     *
     * @param path 请求路径
     * @return 是否是排除路径
     */
    private boolean isExcludePath(String path) {
        for (String excludePath : EXCLUDE_PATHS) {
            if (path.startsWith(excludePath)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 写入错误响应
     *
     * @param response HttpServletResponse
     * @param exception 业务异常
     */
    private void writeErrorResponse(HttpServletResponse response, BusinessException exception) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        
        Result<?> errorResult = Result.error(exception.getErrorCode(), exception.getErrorMessage());
        String json = objectMapper.writeValueAsString(errorResult);
        
        response.getWriter().write(json);
    }
}
