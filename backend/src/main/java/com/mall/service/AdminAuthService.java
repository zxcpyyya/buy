package com.mall.service;

import com.mall.dto.AdminLoginDTO;
import com.mall.vo.AdminUserVO;

import java.util.Map;

/**
 * 后台认证服务接口
 *
 * @author xiu
 */
public interface AdminAuthService {

    /**
     * 后台管理员登录
     *
     * @param loginDTO 登录信息
     * @return token和用户信息
     */
    Map<String, Object> login(AdminLoginDTO loginDTO);

    /**
     * 验证token
     *
     * @param token token
     * @return 用户信息
     */
    AdminUserVO verifyToken(String token);

    /**
     * 刷新token
     *
     * @param token 旧token
     * @return 新token
     */
    String refreshToken(String token);

    /**
     * 退出登录
     *
     * @param token token
     */
    void logout(String token);
}
