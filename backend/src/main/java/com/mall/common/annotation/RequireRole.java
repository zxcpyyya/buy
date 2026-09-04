package com.mall.common.annotation;

import com.mall.common.enums.UserType;
import com.mall.common.exception.BusinessException;

import java.lang.annotation.*;

/**
 * 角色验证注解
 * 标注在Controller方法上，表示该接口需要特定角色
 *
 * @author mall
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {

    /**
     * 角色标识，多个用逗号分隔
     * 如：SUPER_ADMIN, OPERATION_ADMIN
     */
    String[] value() default {};

    /**
     * 角色逻辑：AND表示同时满足，OR表示满足其一
     */
    Logic logic() default Logic.OR;

    enum Logic {
        /**
         * 与：所有角色都要满足
         */
        AND,
        /**
         * 或：满足任一角色即可
         */
        OR
    }

    /**
     * 指定用户类型
     */
    UserType[] userType() default {};
}
