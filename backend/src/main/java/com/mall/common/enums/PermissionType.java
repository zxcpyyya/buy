package com.mall.common.enums;

/**
 * 权限类型枚举
 *
 * @author mall
 */
public enum PermissionType {

    /**
     * 菜单
     */
    MENU(1, "菜单"),

    /**
     * 按钮/操作
     */
    BUTTON(2, "按钮/操作");

    private final int code;
    private final String description;

    PermissionType(int code, String description) {
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
