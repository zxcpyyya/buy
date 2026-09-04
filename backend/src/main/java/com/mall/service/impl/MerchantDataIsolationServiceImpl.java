package com.mall.service.impl;

import com.mall.common.enums.UserType;
import com.mall.context.AdminContext;
import com.mall.service.MerchantDataIsolationService;
import org.springframework.stereotype.Service;

/**
 * 商家数据隔离服务实现
 *
 * @author mall
 */
@Service
public class MerchantDataIsolationServiceImpl implements MerchantDataIsolationService {

    @Override
    public Long getCurrentMerchantId() {
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

    @Override
    public boolean isMerchant() {
        AdminContext.AdminUser admin = AdminContext.getAdmin();
        if (admin == null) {
            return false;
        }
        return admin.getUserType() == UserType.MERCHANT;
    }

    @Override
    public boolean isAdmin() {
        AdminContext.AdminUser admin = AdminContext.getAdmin();
        if (admin == null) {
            return false;
        }

        // 管理员用户类型
        return admin.getUserType() == UserType.ADMIN;
    }

    @Override
    public boolean isSuperAdmin() {
        AdminContext.AdminUser admin = AdminContext.getAdmin();
        if (admin == null) {
            return false;
        }

        // 超管标识
        return Boolean.TRUE.equals(admin.getIsSuperAdmin());
    }
}
