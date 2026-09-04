package com.mall.context;

import com.mall.common.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

/**
 * 后台管理上下文
 * 用于在请求周期内存储当前登录用户信息
 *
 * @author mall
 */
public class AdminContext {

    private static final ThreadLocal<AdminUser> ADMIN_USER = new ThreadLocal<>();

    /**
     * 获取当前登录用户
     */
    public static AdminUser getAdmin() {
        return ADMIN_USER.get();
    }

    /**
     * 设置当前登录用户
     */
    public static void setAdmin(AdminUser admin) {
        ADMIN_USER.set(admin);
    }

    /**
     * 清除上下文
     */
    public static void clear() {
        ADMIN_USER.remove();
    }

    /**
     * 后台管理员用户信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminUser {

        /**
         * 用户ID
         */
        private Long id;

        /**
         * 用户名
         */
        private String username;

        /**
         * 昵称
         */
        private String nickname;

        /**
         * 头像
         */
        private String avatar;

        /**
         * 用户类型
         */
        private UserType userType;

        /**
         * 是否超级管理员
         */
        private Boolean isSuperAdmin;

        /**
         * 商家ID（商家用户才有）
         */
        private Long merchantId;

        /**
         * 商家名称
         */
        private String merchantName;

        /**
         * 角色代码列表
         */
        private List<String> roleCodes;

        /**
         * 权限列表
         */
        private Set<String> permissions;
    }
}
