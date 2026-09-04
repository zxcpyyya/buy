package com.mall.service.impl;

import cn.hutool.core.util.StrUtil;
import com.mall.common.enums.UserType;
import com.mall.service.JwtService;
import com.mall.vo.AdminUserVO;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT Token服务实现类
 *
 * @author mall
 */
@Slf4j
@Service
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.secret:mall-admin-secret-key-2024-secure-random-string}")
    private String secret;

    @Value("${jwt.expire-time:86400}")
    private long expireTime; // 默认24小时

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        // 确保密钥长度足够（至少256位）
        String paddedSecret = secret;
        while (paddedSecret.getBytes(StandardCharsets.UTF_8).length < 32) {
            paddedSecret += secret;
        }
        secretKey = Keys.hmacShaKeyFor(paddedSecret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String generateToken(AdminUserVO userVO) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expireTime * 1000);

        String roles = userVO.getRoles() != null ? String.join(",", userVO.getRoles()) : "";

        return Jwts.builder()
                .subject(String.valueOf(userVO.getId()))
                .claim("userId", userVO.getId())
                .claim("username", userVO.getUsername())
                .claim("userType", userVO.getUserType() != null ? userVO.getUserType().getCode() : UserType.ADMIN.getCode())
                .claim("roles", roles)
                .claim("isSuperAdmin", userVO.getIsSuperAdmin() != null && userVO.getIsSuperAdmin())
                .claim("merchantId", userVO.getMerchantId())
                .issuedAt(now)
                .expiration(expireDate)
                .signWith(secretKey)
                .compact();
    }

    @Override
    public boolean validateToken(String token) {
        if (StrUtil.isBlank(token)) {
            return false;
        }

        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            log.debug("Token验证失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.get("userId", Long.class);
        } catch (Exception e) {
            log.debug("解析Token失败: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public UserType getUserTypeFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Integer userTypeCode = claims.get("userType", Integer.class);
            if (userTypeCode != null) {
                return UserType.fromCode(userTypeCode);
            }
        } catch (Exception e) {
            log.debug("解析Token用户类型失败: {}", e.getMessage());
        }
        return UserType.ADMIN;
    }

    @Override
    public Long getExpireTimeFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Date expireDate = claims.getExpiration();
            if (expireDate != null) {
                long remaining = (expireDate.getTime() - System.currentTimeMillis()) / 1000;
                return Math.max(0, remaining);
            }
        } catch (Exception e) {
            log.debug("解析Token过期时间失败: {}", e.getMessage());
        }
        return 0L;
    }
}
