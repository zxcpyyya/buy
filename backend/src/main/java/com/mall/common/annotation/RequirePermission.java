package com.mall.common.annotation;

import java.lang.annotation.*;

/**
 * 权限验证注解
 * 标注在Controller方法上，表示该接口需要特定权限
 *
 * @author mall
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {

    /**
     * 权限标识，多个用逗号分隔
     * 如：product:add, product:edit
     */
    String[] value() default {};

    /**
     * 权限逻辑：AND表示同时满足，OR表示满足其一
     */
    Logic logic() default Logic.OR;

    /**
     * 用户类型要求
     */
    UserType[] userType() default {};

    enum Logic {
        /**
         * 与：所有权限都满足
         */
        AND,
        /**
         * 或：满足任一权限即可
         */
        OR
    }
}
