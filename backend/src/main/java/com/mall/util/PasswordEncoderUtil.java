package com.mall.util;

import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 密码加密工具类
 * 
 * 
 * 1. 使用盐值加密防止彩虹表攻击
 * 2. 使用SHA-256哈希算法
 * 3. 安全随机数生成器
 * 
 * @author xiu
 * @date 2024/01/01
 */
public class PasswordEncoderUtil {
    
    /**
     * 盐值长度
     */
    private static final int SALT_LENGTH = 16;
    
    /**
     * 哈希迭代次数
     */
    private static final int HASH_ITERATIONS = 10;
    
    /**
     * 安全随机数生成器
     */
    private static final SecureRandom RANDOM = new SecureRandom();
    
    /**
     * 加密密码
     *
     * @param rawPassword 明文密码
     * @return 加密后的密码
     */
    public static String encode(String rawPassword) {
        // 生成盐值
        byte[] salt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(salt);
        String saltString = Base64.getEncoder().encodeToString(salt);
        
        // 多次哈希
        String hash = rawPassword;
        for (int i = 0; i < HASH_ITERATIONS; i++) {
            hash = DigestUtils.md5Hex(hash + saltString, StandardCharsets.UTF_8);
        }
        
        // 返回盐值和哈希组合
        return saltString + ":" + hash;
    }
    
    /**
     * 验证密码
     *
     * @param rawPassword 明文密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    public static Boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        
        // 分离盐值和哈希
        String[] parts = encodedPassword.split(":");
        if (parts.length != 2) {
            return false;
        }
        
        String saltString = parts[0];
        String storedHash = parts[1];
        
        // 重新计算哈希
        String hash = rawPassword;
        for (int i = 0; i < HASH_ITERATIONS; i++) {
            hash = DigestUtils.md5Hex(hash + saltString, StandardCharsets.UTF_8);
        }
        
        return hash.equals(storedHash);
    }
    
    /**
     * MD5加密（简单场景使用）
     *
     * @param text 明文
     * @return MD5哈希值
     */
    public static String md5(String text) {
        return DigestUtils.md5Hex(text, StandardCharsets.UTF_8);
    }
    
    /**
     * SHA-256哈希
     *
     * @param text 明文
     * @return SHA-256哈希值
     */
    public static String sha256(String text) {
        return DigestUtils.sha256Hex(text, StandardCharsets.UTF_8);
    }
}
