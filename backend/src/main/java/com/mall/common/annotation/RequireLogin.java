package com.mall.common.annotation;

import java.lang.annotation.*;

/**
 * 登录验证注解
 * 标注在Controller或方法上，表示需要登录才能访问
 *
 * @author mall
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireLogin {

    /**
     * 是否需要登录，默认true
     */
    boolean required() default true;
}
