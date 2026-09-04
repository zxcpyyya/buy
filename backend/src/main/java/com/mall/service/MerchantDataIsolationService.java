package com.mall.service;

import com.mall.common.enums.UserType;
import com.mall.context.AdminContext;

/**
 * 商家数据隔离服务
 * 用于在查询时自动添加商家ID过滤条件
 *
 * @author mall
 */
public interface MerchantDataIsolationService {

    /**
     * 获取当前用户的商家ID
     * 如果不是商家用户，返回null
     */
    Long getCurrentMerchantId();

    /**
     * 检查当前用户是否是商家
     */
    boolean isMerchant();

    /**
     * 检查当前用户是否是管理员
     */
    boolean isAdmin();

    /**
     * 检查当前用户是否是超管
     */
    boolean isSuperAdmin();
}
