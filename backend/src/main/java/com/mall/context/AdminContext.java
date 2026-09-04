package com.mall.context;

import com.mall.common.enums.UserType;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 后台管理员上下文
 * 用于在请求线程中存储当前登录的管理员信息
 *
 * @author mall
 */
@Data
public class AdminContext implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final ThreadLocal<AdminUser> ADMIN_USER = new ThreadLocal<>();

    /**
     * 设置当前管理员
     */
    public static void setAdmin(AdminUser admin) {
        ADMIN_USER.set(admin);
    }

    /**
     * 获取当前管理员
     */
    public static AdminUser getAdmin() {
        return ADMIN_USER.get();
    }

    /**
     * 获取当前管理员ID
     */
    public static Long getAdminId() {
        AdminUser admin = ADMIN_USER.get();
        return admin != null ? admin.getId() : null;
    }

    /**
     * 获取当前管理员用户名
     */
    public static String getUsername() {
        AdminUser admin = ADMIN_USER.get();
        return admin != null ? admin.getUsername() : null;
    }

    /**
     * 清空上下文
     */
    public static void clear() {
        ADMIN_USER.remove();
    }

    /**
     * 管理员用户信息
     */
    @Data
    public static class AdminUser implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * 管理员ID
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
         * 角色ID列表
         */
        private java.util.List<Long> roleIds;

        /**
         * 角色标识列表
         */
        private java.util.List<String> roleCodes;

        /**
         * 权限标识列表
         */
        private java.util.List<String> permissions;

        /**
         * 商家ID（如果是商家用户）
         */
        private Long merchantId;

        /**
         * 用户类型
         */
        private UserType userType;

        /**
         * 是否超管
         */
        private Boolean isSuperAdmin;
    }
}
