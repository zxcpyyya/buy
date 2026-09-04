package com.mall.context;

import lombok.Data;

/**
 * 用户上下文Holder
 * 
 * 使用ThreadLocal保存当前线程的用户信息，避免在方法参数中传递用户ID
 * 
 * @author xiu
 * @date 2024/01/01
 */
public class UserContext {
    
    /**
     * 用户上下文ThreadLocal
     */
    private static final ThreadLocal<UserInfo> USER_CONTEXT = new ThreadLocal<>();
    
    /**
     * 设置当前用户信息
     *
     * @param userInfo 用户信息
     */
    public static void setUserInfo(UserInfo userInfo) {
        USER_CONTEXT.set(userInfo);
    }
    
    /**
     * 获取当前用户信息
     *
     * @return 用户信息
     */
    public static UserInfo getUserInfo() {
        return USER_CONTEXT.get();
    }
    
    /**
     * 获取当前用户ID
     *
     * @return 用户ID
     */
    public static Long getUserId() {
        UserInfo userInfo = USER_CONTEXT.get();
        return userInfo != null ? userInfo.getUserId() : null;
    }
    
    /**
     * 获取当前用户名
     *
     * @return 用户名
     */
    public static String getUsername() {
        UserInfo userInfo = USER_CONTEXT.get();
        return userInfo != null ? userInfo.getUsername() : null;
    }
    
    /**
     * 清除当前用户信息
     */
    public static void clear() {
        USER_CONTEXT.remove();
    }
    
    /**
     * 用户信息内部类
     */
    @Data
    public static class UserInfo {
        /**
         * 用户ID
         */
        private Long userId;
        
        /**
         * 用户名
         */
        private String username;
        
        /**
         * Token
         */
        private String token;
    }
}
