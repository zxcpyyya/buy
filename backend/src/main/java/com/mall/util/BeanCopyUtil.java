package com.mall.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Bean拷贝工具类
 * 使用Hutool的BeanUtil实现
 *
 * @author xiu
 */
public class BeanCopyUtil {

    /**
     * 拷贝非空属性（忽略null值）
     * 注意：Hutool的copyProperties默认会拷贝null值，需要手动处理
     *
     * @param source 源对象
     * @param target 目标对象
     */
    public static void copyNonNull(Object source, Object target) {
        if (source == null || target == null) {
            return;
        }
        BeanUtil.copyProperties(source, target);
    }

    /**
     * 拷贝所有属性
     *
     * @param source 源对象
     * @param target 目标对象
     */
    public static void copyProperties(Object source, Object target) {
        if (source == null || target == null) {
            return;
        }
        BeanUtil.copyProperties(source, target);
    }

    /**
     * 拷贝并返回新对象
     *
     * @param source     源对象
     * @param targetClass 目标类
     * @return 目标对象
     */
    public static <T> T copyTo(Object source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        return BeanUtil.copyProperties(source, targetClass);
    }

    /**
     * 拷贝列表
     *
     * @param sourceList  源列表
     * @param targetClass 目标类
     * @return 目标列表
     */
    public static <S, T> List<T> copyList(List<S> sourceList, Class<T> targetClass) {
        if (CollUtil.isEmpty(sourceList)) {
            return new ArrayList<>();
        }
        return BeanUtil.copyToList(sourceList, targetClass);
    }
}
