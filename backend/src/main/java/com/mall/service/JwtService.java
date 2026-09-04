package com.mall.service;

import com.mall.common.enums.UserType;
import com.mall.vo.AdminUserVO;

/**
 * JWT Token服务接口
 *
 * @author mall
 */
public interface JwtService {

    /**
     * 生成Token
     *
     * @param userVO 用户信息
     * @return token
     */
    String generateToken(AdminUserVO userVO);

    /**
     * 验证Token
     *
     * @param token token
     * @return 是否有效
     */
    boolean validateToken(String token);

    /**
     * 从Token获取用户ID
     *
     * @param token token
     * @return 用户ID
     */
    Long getUserIdFromToken(String token);

    /**
     * 从Token获取用户类型
     *
     * @param token token
     * @return 用户类型
     */
    UserType getUserTypeFromToken(String token);

    /**
     * 获取Token过期时间（秒）
     *
     * @param token token
     * @return 剩余过期时间
     */
    Long getExpireTimeFromToken(String token);
}
