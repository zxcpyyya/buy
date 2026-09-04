package com.mall.common.annotation;

import com.mall.common.enums.UserType;
import java.lang.annotation.*;

/**
 * 角色验证注解
 * 标注在Controller或方法上，表示需要特定角色才能访问
 *
 * @author mall
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {

    /**
     * 允许访问的角色列表
     */
    String[] value() default {};

    /**
     * 用户类型限制
     */
    UserType[] userType() default {};

    /**
     * 逻辑运算方式
     */
    Logic logic() default Logic.OR;

    /**
     * 逻辑运算
     */
    enum Logic {
        /**
         * 或：满足任意一个角色即可
         */
        OR,

        /**
         * 与：需要满足所有角色
         */
        AND
    }
}
