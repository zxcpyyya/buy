package com.mall.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户类型枚举
 *
 * @author mall
 */
@Getter
@AllArgsConstructor
public enum UserType {

    /**
     * 管理员用户
     */
    ADMIN(1, "管理员"),

    /**
     * 商家用户
     */
    MERCHANT(2, "商家");

    private final int code;
    private final String desc;
}
