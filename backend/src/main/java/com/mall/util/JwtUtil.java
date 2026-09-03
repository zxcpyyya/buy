package com.mall.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 
 * 遵循阿里Java开发规范：
 * 1. 使用SLF4J日志框架
 * 2. 异常处理完善
 * 3. 常量不出现魔法值
 * 
 * @author mall
 * @date 2024/01/01
 */
@Slf4j
@Component
public class JwtUtil {
    
    /**
     * JWT密钥
     */
    @Value("${jwt.secret}")
    private String secret;
    
    /**
     * 过期时间（毫秒）
     */
    @Value("${jwt.expiration}")
    private Long expiration;
    
    /**
     * 生成JWT Token
     *
     * @param userId 用户ID
     * @param username 用户名
     * @return JWT Token
     */
    public String generateToken(Long userId, String username) {
        // 创建Token Claims
        Map<String, Object> claims = new HashMap<>(2);
        claims.put("userId", userId);
        claims.put("username", username);
        
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration);
        
        // 生成签名密钥
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        
        // 构建Token
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(key)
                .compact();
    }
    
    /**
     * 验证Token是否有效
     *
     * @param token JWT Token
     * @return 是否有效
     */
    public Boolean validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT Token已过期, errorMessage={}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("JWT Token格式不支持, errorMessage={}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("JWT Token格式错误, errorMessage={}", e.getMessage());
        } catch (SecurityException e) {
            log.warn("JWT签名验证失败, errorMessage={}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT Token为空或为空字符串, errorMessage={}", e.getMessage());
        }
        return false;
    }
    
    /**
     * 从Token中获取用户ID
     *
     * @param token JWT Token
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = this.getClaimsFromToken(token);
        if (claims == null) {
            return null;
        }
        
        Object userId = claims.get("userId");
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        } else if (userId instanceof Long) {
            return (Long) userId;
        }
        
        return null;
    }
    
    /**
     * 从Token中获取用户名
     *
     * @param token JWT Token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = this.getClaimsFromToken(token);
        if (claims == null) {
            return null;
        }
        return claims.getSubject();
    }
    
    /**
     * 从Token中获取Claims
     *
     * @param token JWT Token
     * @return Claims
     */
    private Claims getClaimsFromToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("JWT Token已过期, errorMessage={}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("JWT Token格式不支持, errorMessage={}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("JWT Token格式错误, errorMessage={}", e.getMessage());
        } catch (SecurityException e) {
            log.warn("JWT签名验证失败, errorMessage={}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT Token为空或为空字符串, errorMessage={}", e.getMessage());
        }
        return null;
    }
    
    /**
     * 判断Token是否即将过期（提前5分钟）
     *
     * @param token JWT Token
     * @return 是否即将过期
     */
    public Boolean isTokenExpiringSoon(String token) {
        Claims claims = this.getClaimsFromToken(token);
        if (claims == null) {
            return true;
        }
        
        Date expiration = claims.getExpiration();
        long timeToExpire = expiration.getTime() - System.currentTimeMillis();
        
        // 提前5分钟（300000毫秒）认为即将过期
        return timeToExpire < 300000L;
    }
    
    /**
     * 刷新Token
     *
     * @param oldToken 旧Token
     * @return 新Token
     */
    public String refreshToken(String oldToken) {
        Claims claims = this.getClaimsFromToken(oldToken);
        if (claims == null) {
            return null;
        }
        
        Long userId = this.getUserIdFromToken(oldToken);
        String username = this.getUsernameFromToken(oldToken);
        
        return this.generateToken(userId, username);
    }
}
