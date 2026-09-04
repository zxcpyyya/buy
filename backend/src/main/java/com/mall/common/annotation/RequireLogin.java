package com.mall.common.annotation;

import java.lang.annotation.*;

/**
 * 登录验证注解
 * 标注在Controller方法上，表示该接口需要登录
 *
 * @author mall
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireLogin {

    /**
     * 是否强制要求登录，默认true
     * 设置为false时，有token会验证，无token也放行
     */
    boolean required() default true;
}
