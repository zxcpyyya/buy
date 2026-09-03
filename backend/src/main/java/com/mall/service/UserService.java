package com.mall.service;

/**
 * 用户Service接口
 * 
 * @author mall
 * @date 2024/01/01
 */
public interface UserService {
    
    /**
     * 用户注册
     *
     * @param registerDTO 注册参数
     * @return 用户ID
     */
    Long register(RegisterDTO registerDTO);
    
    /**
     * 用户登录
     *
     * @param loginDTO 登录参数
     * @return JWT Token
     */
    String login(LoginDTO loginDTO);
    
    /**
     * 获取当前登录用户信息
     *
     * @param userId 用户ID
     * @return 用户VO
     */
    UserVO getCurrentUser(Long userId);
    
    /**
     * 更新用户信息
     *
     * @param userId 用户ID
     * @param updateDTO 更新参数
     * @return 是否成功
     */
    Boolean updateUserInfo(Long userId, UpdateUserDTO updateDTO);
    
    /**
     * 修改密码
     *
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否成功
     */
    Boolean changePassword(Long userId, String oldPassword, String newPassword);
    
    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户DO
     */
    UserDO findByUsername(String username);
}
