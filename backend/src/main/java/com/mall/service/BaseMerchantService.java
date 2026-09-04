package com.mall.service;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.enums.UserType;
import com.mall.context.AdminContext;

/**
 * 商家数据隔离Service基类
 * 所有需要商家数据隔离的Service继承此类
 *
 * @param <M> Mapper
 * @param <T> 实体
 * @author mall
 */
public abstract class BaseMerchantService<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> {

    /**
     * 获取当前用户的商家ID
     * 如果不是商家用户，返回null（表示不过滤）
     */
    protected Long getMerchantId() {
        AdminContext.AdminUser admin = AdminContext.getAdmin();
        if (admin == null) {
            return null;
        }

        // 只有商家用户才有merchantId
        if (admin.getUserType() == UserType.MERCHANT) {
            return admin.getMerchantId();
        }

        return null;
    }

    /**
     * 检查当前用户是否是商家
     */
    protected boolean isMerchant() {
        AdminContext.AdminUser admin = AdminContext.getAdmin();
        if (admin == null) {
            return false;
        }
        return admin.getUserType() == UserType.MERCHANT;
    }

    /**
     * 检查当前用户是否是管理员
     */
    protected boolean isAdmin() {
        AdminContext.AdminUser admin = AdminContext.getAdmin();
        if (admin == null) {
            return false;
        }
        return admin.getUserType() == UserType.ADMIN;
    }

    /**
     * 检查当前用户是否是超管
     */
    protected boolean isSuperAdmin() {
        AdminContext.AdminUser admin = AdminContext.getAdmin();
        if (admin == null) {
            return false;
        }
        return Boolean.TRUE.equals(admin.getIsSuperAdmin());
    }

    /**
     * 获取当前用户ID
     */
    protected Long getUserId() {
        AdminContext.AdminUser admin = AdminContext.getAdmin();
        return admin != null ? admin.getId() : null;
    }
}
