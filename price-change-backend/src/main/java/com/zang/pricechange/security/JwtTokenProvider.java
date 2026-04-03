package com.zang.pricechange.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT Token 工具类
 * 负责生成、解析和验证 JWT Token
 */
@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${jwt.secret:price-change-secret-key-for-jwt-token-generation-must-be-at-least-256-bits}")
    private String secretString;

    @Value("${jwt.expiration:7200000}")
    private long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 JWT Token
     */
    public String generateToken(String username, Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 从 Token 中获取用户名
     */
    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * 从 Token 中获取用户 ID
     */
    public Long getUserIdFromToken(String token) {
        return parseClaims(token).get("userId", Long.class);
    }

    /**
     * 验证 Token 是否有效
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT Token 已过期: {}", e.getMessage());
            return false;
        } catch (JwtException e) {
            log.warn("JWT Token 无效: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.warn("JWT Token 为空或格式错误");
            return false;
        }
    }

    /**
     * 解析 Token 获取 Claims（公共方法，避免重复代码）
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
