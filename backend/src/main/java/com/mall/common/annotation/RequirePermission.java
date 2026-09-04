package com.mall.common.annotation;

import java.lang.annotation.*;

/**
 * 权限验证注解
 * 标注在Controller或方法上，表示需要特定权限才能访问
 *
 * @author mall
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {

    /**
     * 允许访问的权限列表
     */
    String[] value() default {};

    /**
     * 逻辑运算方式
     */
    Logic logic() default Logic.OR;

    /**
     * 逻辑运算
     */
    enum Logic {
        /**
         * 或：满足任意一个权限即可
         */
        OR,

        /**
         * 与：需要满足所有权限
         */
        AND
    }
}
