package com.mall.service;

import com.mall.vo.GithubLoginVO;

/**
 * GitHub 登录服务接口
 *
 * @author system
 * @date 2026/09/15
 */
public interface GithubLoginService {

    /**
     * 获取 GitHub 授权 URL
     *
     * @param state 随机状态码（防CSRF）
     * @return 授权跳转 URL
     */
    String getAuthorizeUrl(String state);

    /**
     * GitHub 授权回调处理
     *
     * @param code  授权码
     * @param state 状态码
     * @return 登录结果
     */
    GithubLoginVO callback(String code, String state);

    /**
     * 根据 GitHub ID 获取或创建用户
     *
     * @param githubId       GitHub 用户ID
     * @param githubUsername GitHub 用户名
     * @param avatar         头像 URL
     * @param email          邮箱
     * @return 登录结果
     */
    GithubLoginVO getOrCreateUser(String githubId, String githubUsername, String avatar, String email);
}
