package com.mall.interceptor;

import cn.hutool.core.util.StrUtil;
import com.mall.common.annotation.RequireLogin;
import com.mall.common.annotation.RequirePermission;
import com.mall.common.annotation.RequireRole;
import com.mall.common.enums.UserType;
import com.mall.common.exception.BusinessException;
import com.mall.common.ratelimit.TokenBucketLimiter;
import com.mall.context.AdminContext;
import com.mall.service.AdminAuthService;
import com.mall.service.JwtService;
import com.mall.vo.AdminUserVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 权限验证拦截器
 * 1. 解析Token并设置上下文
 * 2. 验证用户登录状态
 * 3. 验证用户角色
 * 4. 验证用户权限
 * 5. IP追踪和限流
 *
 * @author xiu
 */
@Slf4j
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AdminAuthService adminAuthService;

    /**
     * 登录接口限流配置
     */
    private static final int LOGIN_RATE = 5;   // 每秒5个请求
    private static final int LOGIN_CAPACITY = 10; // 突发容量10

    /**
     * 普通接口限流配置
     */
    private static final int NORMAL_RATE = 30;  // 每秒30个请求
    private static final int NORMAL_CAPACITY = 60; // 突发容量60

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如果不是HandlerMethod，直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String clientIp = getClientIp(request);
        String uri = request.getRequestURI();

        // 1. IP限流检查
        if (!checkRateLimit(clientIp, uri)) {
            log.warn("IP限流触发: ip={}, uri={}", clientIp, uri);
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":429,\"message\":\"请求过于频繁，请稍后再试\"}");
            return false;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // 获取类和方法上的注解
        RequireLogin classLogin = handlerMethod.getBeanType().getAnnotation(RequireLogin.class);
        RequireLogin methodLogin = handlerMethod.getMethodAnnotation(RequireLogin.class);
        RequireLogin requireLogin = methodLogin != null ? methodLogin : classLogin;

        RequireRole classRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        RequireRole methodRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        RequireRole requireRole = methodRole != null ? methodRole : classRole;

        RequirePermission classPerm = handlerMethod.getBeanType().getAnnotation(RequirePermission.class);
        RequirePermission methodPerm = handlerMethod.getMethodAnnotation(RequirePermission.class);
        RequirePermission requirePerm = methodPerm != null ? methodPerm : classPerm;

        // 1. 解析Token并设置上下文
        String token = extractToken(request);
        AdminContext.AdminUser adminUser = null;

        if (StrUtil.isNotBlank(token)) {
            try {
                adminUser = buildAdminUser(token);
                AdminContext.setAdmin(adminUser);
            } catch (Exception e) {
                log.debug("Token解析失败: {}", e.getMessage());
            }
        }

        // 2. 检查是否需要登录
        if (requireLogin != null && requireLogin.required()) {
            if (adminUser == null) {
                throw BusinessException.of(401, "请先登录");
            }
        } else if (requireLogin == null && adminUser == null) {
            // 没有RequireLogin注解且没有登录，直接放行（公开接口）
            return true;
        }

        // 3. 如果已登录，验证用户类型
        if (adminUser != null) {
            // 验证用户类型
            if (requireRole != null && requireRole.userType().length > 0) {
                boolean typeMatch = Arrays.stream(requireRole.userType())
                        .anyMatch(t -> t == adminUser.getUserType());
                if (!typeMatch) {
                    throw BusinessException.of(403, "无权访问该接口");
                }
            }

            // 验证角色
            if (requireRole != null && requireRole.value().length > 0) {
                boolean hasRole = false;
                List<String> userRoles = adminUser.getRoleCodes();

                if (adminUser.getIsSuperAdmin()) {
                    // 超管拥有所有角色
                    hasRole = true;
                } else if (userRoles != null) {
                    if (requireRole.logic() == RequireRole.Logic.AND) {
                        // 与：需要满足所有角色
                        hasRole = Arrays.stream(requireRole.value())
                                .allMatch(role -> userRoles.contains(role));
                    } else {
                        // 或：只需要满足任一角色
                        hasRole = Arrays.stream(requireRole.value())
                                .anyMatch(role -> userRoles.contains(role));
                    }
                }

                if (!hasRole) {
                    throw BusinessException.of(403, "您没有该角色权限");
                }
            }

            // 验证权限
            if (requirePerm != null && requirePerm.value().length > 0) {
                boolean hasPermission = false;
                Set<String> userPerms = adminUser.getPermissions() != null ?
                        adminUser.getPermissions().stream().collect(Collectors.toSet()) : null;

                if (adminUser.getIsSuperAdmin()) {
                    // 超管拥有所有权限
                    hasPermission = true;
                } else if (userPerms != null) {
                    if (requirePerm.logic() == RequirePermission.Logic.AND) {
                        // 与：需要满足所有权限
                        hasPermission = Arrays.stream(requirePerm.value())
                                .allMatch(perm -> userPerms.contains(perm));
                    } else {
                        // 或：只需要满足任一权限
                        hasPermission = Arrays.stream(requirePerm.value())
                                .anyMatch(perm -> userPerms.contains(perm));
                    }
                }

                if (!hasPermission) {
                    throw BusinessException.of(403, "您没有该操作权限");
                }
            }
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 请求完成后清空上下文
        AdminContext.clear();
    }

    /**
     * 从请求中提取Token
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StrUtil.isNotBlank(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 构建AdminUser上下文
     */
    private AdminContext.AdminUser buildAdminUser(String token) {
        // 从Token中解析基本信息
        Long userId = jwtService.getUserIdFromToken(token);
        if (userId == null) {
            return null;
        }

        // 从数据库获取完整用户信息
        AdminUserVO userVO = adminAuthService.verifyToken(token);

        AdminContext.AdminUser adminUser = new AdminContext.AdminUser();
        adminUser.setId(userVO.getId());
        adminUser.setUsername(userVO.getUsername());
        adminUser.setNickname(userVO.getNickname());
        adminUser.setRoleCodes(userVO.getRoles());
        adminUser.setPermissions(userVO.getPermissions());
        adminUser.setIsSuperAdmin(userVO.getIsSuperAdmin());
        adminUser.setMerchantId(userVO.getMerchantId());
        adminUser.setUserType(userVO.getUserType() != null ? userVO.getUserType() : UserType.ADMIN);

        return adminUser;
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
     * IP限流检查
     */
    private boolean checkRateLimit(String clientIp, String uri) {
        // 检查IP是否被封禁
        if (TokenBucketLimiter.isBlocked(clientIp)) {
            log.warn("IP被封禁: ip={}", clientIp);
            return false;
        }

        // 登录接口特殊限流
        if (isLoginEndpoint(uri)) {
            return TokenBucketLimiter.tryAcquire("login:" + clientIp, LOGIN_RATE, LOGIN_CAPACITY);
        }

        // 普通接口限流
        return TokenBucketLimiter.tryAcquire("api:" + clientIp, NORMAL_RATE, NORMAL_CAPACITY);
    }

    /**
     * 判断是否为登录接口
     */
    private boolean isLoginEndpoint(String uri) {
        return uri.contains("/login") || uri.contains("/auth/login");
    }
}
