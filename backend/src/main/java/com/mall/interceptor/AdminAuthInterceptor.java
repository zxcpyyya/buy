package com.mall.interceptor;

import cn.hutool.core.util.StrUtil;
import com.mall.common.annotation.RequireLogin;
import com.mall.common.annotation.RequirePermission;
import com.mall.common.annotation.RequireRole;
import com.mall.common.enums.UserType;
import com.mall.common.exception.BusinessException;
import com.mall.context.AdminContext;
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
 * 后台权限验证拦截器
 * 1. 解析Token并设置上下文
 * 2. 验证用户登录状态
 * 3. 验证用户角色
 * 4. 验证用户权限
 * 5. 商家数据隔离
 *
 * @author mall
 */
@Slf4j
public class AdminAuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtService jwtService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如果不是HandlerMethod，直接放行
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

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

        // 2. 获取注解配置
        RequireLogin requireLogin = getAnnotation(handlerMethod, RequireLogin.class);
        RequireRole requireRole = getAnnotation(handlerMethod, RequireRole.class);
        RequirePermission requirePerm = getAnnotation(handlerMethod, RequirePermission.class);

        // 3. 检查是否需要登录
        if (requireLogin != null && requireLogin.required()) {
            if (adminUser == null) {
                throw BusinessException.of(401, "请先登录");
            }
        } else if (requireLogin == null && adminUser == null) {
            // 没有RequireLogin注解且没有登录，直接放行
            return true;
        }

        // 4. 如果已登录，验证权限
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
                boolean hasRole = checkRole(adminUser, requireRole);
                if (!hasRole) {
                    throw BusinessException.of(403, "您没有该角色权限");
                }
            }

            // 验证权限
            if (requirePerm != null && requirePerm.value().length > 0) {
                boolean hasPermission = checkPermission(adminUser, requirePerm);
                if (!hasPermission) {
                    throw BusinessException.of(403, "您没有该操作权限");
                }
            }

            // 5. 商家数据隔离检查
            checkMerchantIsolation(request, adminUser);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
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
        Long userId = jwtService.getUserIdFromToken(token);
        if (userId == null) {
            return null;
        }

        AdminContext.AdminUser adminUser = new AdminContext.AdminUser();
        adminUser.setId(userId);

        // 从token中获取基本信息
        UserType userType = jwtService.getUserTypeFromToken(token);
        adminUser.setUserType(userType != null ? userType : UserType.ADMIN);

        // 注意：完整信息由Controller层从数据库获取
        return adminUser;
    }

    /**
     * 检查角色
     */
    private boolean checkRole(AdminContext.AdminUser admin, RequireRole requireRole) {
        // 超管拥有所有角色
        if (Boolean.TRUE.equals(admin.getIsSuperAdmin())) {
            return true;
        }

        List<String> userRoles = admin.getRoleCodes();
        if (userRoles == null || userRoles.isEmpty()) {
            return false;
        }

        String[] requiredRoles = requireRole.value();
        if (requireRole.logic() == RequireRole.Logic.AND) {
            // 与：需要满足所有角色
            return Arrays.stream(requiredRoles).allMatch(role -> userRoles.contains(role));
        } else {
            // 或：只需要满足任一角色
            return Arrays.stream(requiredRoles).anyMatch(role -> userRoles.contains(role));
        }
    }

    /**
     * 检查权限
     */
    private boolean checkPermission(AdminContext.AdminUser admin, RequirePermission requirePerm) {
        // 超管拥有所有权限
        if (Boolean.TRUE.equals(admin.getIsSuperAdmin())) {
            return true;
        }

        Set<String> userPerms = admin.getPermissions() != null ?
                admin.getPermissions().stream().collect(Collectors.toSet()) : null;

        if (userPerms == null || userPerms.isEmpty()) {
            return false;
        }

        String[] requiredPerms = requirePerm.value();
        if (requirePerm.logic() == RequirePermission.Logic.AND) {
            return Arrays.stream(requiredPerms).allMatch(perm -> userPerms.contains(perm));
        } else {
            return Arrays.stream(requiredPerms).anyMatch(perm -> userPerms.contains(perm));
        }
    }

    /**
     * 商家数据隔离检查
     * 商家只能访问自己店铺的数据
     */
    private void checkMerchantIsolation(HttpServletRequest request, AdminContext.AdminUser admin) {
        // 只有商家用户需要数据隔离
        if (admin.getUserType() != UserType.MERCHANT) {
            return;
        }

        String path = request.getRequestURI();
        String method = request.getMethod();

        // 商家禁止访问的接口
        List<String> forbiddenPaths = List.of(
                "/api/admin/user-list",
                "/api/admin/admin-list",
                "/api/admin/role-list",
                "/api/admin/merchant-list",
                "/api/admin/sales-stat",
                "/api/admin/user-stat",
                "/api/admin/points-rule"
        );

        for (String forbiddenPath : forbiddenPaths) {
            if (path.startsWith(forbiddenPath)) {
                throw BusinessException.of(403, "您没有权限访问该功能");
            }
        }

        // 在请求中添加商家ID，用于后续数据过滤
        request.setAttribute("merchantId", admin.getMerchantId());
    }

    /**
     * 获取方法或类上的注解
     */
    @SuppressWarnings("unchecked")
    private <T> T getAnnotation(HandlerMethod handlerMethod, Class<T> annotationClass) {
        // 先检查方法注解
        T annotation = handlerMethod.getMethodAnnotation(annotationClass);
        if (annotation != null) {
            return annotation;
        }
        // 再检查类注解
        return (T) handlerMethod.getBeanType().getAnnotation(annotationClass);
    }
}
