package com.mall.interceptor;

import cn.hutool.core.util.StrUtil;
import com.mall.common.annotation.RequireLogin;
import com.mall.common.annotation.RequirePermission;
import com.mall.common.annotation.RequireRole;
import com.mall.common.enums.UserType;
import com.mall.common.exception.BusinessException;
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
 *
 * @author mall
 */
@Slf4j
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AdminAuthService adminAuthService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如果不是HandlerMethod，直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
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
}
