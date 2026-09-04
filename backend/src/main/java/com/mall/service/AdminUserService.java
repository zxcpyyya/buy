package com.mall.service;

import com.mall.vo.MenuVO;
import com.mall.vo.AdminUserVO;

import java.util.List;

/**
 * 后台用户服务接口
 *
 * @author mall
 */
public interface AdminUserService {

    /**
     * 获取当前用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    AdminUserVO getUserInfo(Long userId);

    /**
     * 获取用户菜单权限
     *
     * @param userId 用户ID
     * @return 菜单列表
     */
    List<MenuVO> getUserMenus(Long userId);

    /**
     * 获取用户按钮权限
     *
     * @param userId 用户ID
     * @return 权限标识列表
     */
    List<String> getUserPermissions(Long userId);

    /**
     * 修改密码
     *
     * @param userId      用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否成功
     */
    Boolean changePassword(Long userId, String oldPassword, String newPassword);
}
