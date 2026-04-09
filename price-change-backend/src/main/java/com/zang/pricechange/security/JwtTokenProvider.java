package com.zang.pricechange.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT Token 工具类
 * 负责生成、解析和验证 JWT Token
 */
@Slf4j
@Component
public class JwtTokenProvider {

    /**
     * JWT 签名密钥
     * <p>
     * 从配置文件读取 jwt.secret，如果未配置则使用默认值
     * ⚠️ 生产环境必须配置足够长度的随机密钥（至少 256 位）
     */
    @Value("${jwt.secret:price-change-secret-key-for-jwt-token-generation-must-be-at-least-256-bits}")
    private String secretString;

    /**
     * Token 过期时间（毫秒）
     * <p>
     * 默认值：7200000 毫秒 = 2 小时
     * 可通过配置文件 jwt.expiration 自定义
     */
    @Value("${jwt.expiration:7200000}")
    private long expiration;

    /**
     * 获取签名密钥
     * <p>
     * 将字符串密钥转换为 HMAC-SHA 算法所需的 SecretKey 对象
     *
     * @return HMAC-SHA 签名密钥
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 JWT Token
     * <p>
     * 包含以下信息：
     * - subject: 用户名
     * - userId: 用户 ID（自定义 claim）
     * - issuedAt: 签发时间
     * - expiration: 过期时间
     *
     * @param username 用户名
     * @param userId   用户 ID
     * @return 生成的 JWT Token 字符串
     */
    public String generateToken(String username, Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(username)                    // 设置主题（用户名）
                .claim("userId", userId)              // 添加自定义声明（用户 ID）
                .issuedAt(now)                        // 设置签发时间
                .expiration(expiryDate)               // 设置过期时间
                .signWith(getSigningKey())            // 使用密钥签名
                .compact();                           // 压缩生成最终 Token
    }

    /**
     * 从 Token 中获取用户名
     *
     * @param token JWT Token 字符串
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * 从 Token 中获取用户 ID
     *
     * @param token JWT Token 字符串
     * @return 用户 ID
     */
    public Long getUserIdFromToken(String token) {
        return parseClaims(token).get("userId", Long.class);
    }

    /**
     * 验证 Token 是否有效
     * <p>
     * 检查项：
     * 1. Token 格式是否正确
     * 2. Token 签名是否有效
     * 3. Token 是否已过期
     *
     * @param token JWT Token 字符串
     * @return true-有效，false-无效
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            // Token 已过期
            log.warn("JWT Token 已过期: {}", e.getMessage());
            return false;
        } catch (JwtException e) {
            // Token 签名无效或格式错误
            log.warn("JWT Token 无效: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            // Token 为空或格式不正确
            log.warn("JWT Token 为空或格式错误");
            return false;
        }
    }

    /**
     * 解析 Token 获取 Claims（公共方法，避免重复代码）
     * <p>
     * 统一处理 Token 解析逻辑，提取 payload 中的声明信息
     *
     * @param token JWT Token 字符串
     * @return Claims 对象，包含 Token 中的所有声明
     * @throws ExpiredJwtException    Token 已过期
     * @throws JwtException           Token 无效
     * @throws IllegalArgumentException Token 为空或格式错误
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())        // 设置验证密钥
                .build()                             // 构建解析器
                .parseSignedClaims(token)            // 解析并验证签名
                .getPayload();                       // 获取 payload（声明内容）
    }
}
