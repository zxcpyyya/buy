package com.mall.util;

import cn.hutool.crypto.digest.DigestUtil;
import org.springframework.stereotype.Component;

/**
 * 密码加密工具类
 * 
 * @author mall
 * @date 2024/01/01
 */
@Component
public class PasswordEncoder {
    
    /**
     * 加密密码
     * 使用SHA-256加密，添加盐值
     * 
     * @param rawPassword 原始密码
     * @return 加密后的密码
     */
    public String encode(String rawPassword) {
        return DigestUtil.sha256Hex("mall_salt_" + rawPassword + "_2024");
    }
    
    /**
     * 验证密码
     * 
     * @param rawPassword 原始密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        return this.encode(rawPassword).equals(encodedPassword);
    }
}
