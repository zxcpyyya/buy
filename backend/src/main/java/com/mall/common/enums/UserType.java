package com.mall.common.enums;

/**
 * 用户类型枚举
 *
 * @author mall
 */
public enum UserType {

    /**
     * C端用户（消费者）
     */
    CUSTOMER(1, "C端用户"),

    /**
     * 后台管理员
     */
    ADMIN(2, "后台管理员"),

    /**
     * 商家用户
     */
    MERCHANT(3, "商家用户");

    private final int code;
    private final String description;

    UserType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static UserType fromCode(int code) {
        for (UserType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }
}
