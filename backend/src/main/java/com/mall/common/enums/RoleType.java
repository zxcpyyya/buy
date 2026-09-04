package com.mall.common.enums;

/**
 * 角色类型枚举
 *
 * @author xiu
 */
public enum RoleType {

    /**
     * 后台管理员
     */
    ADMIN(1, "后台管理员"),

    /**
     * 商家
     */
    MERCHANT(2, "商家");

    private final int code;
    private final String description;

    RoleType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
