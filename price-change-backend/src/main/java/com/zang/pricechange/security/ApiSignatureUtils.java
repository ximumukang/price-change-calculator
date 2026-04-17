package com.zang.pricechange.security;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.TreeMap;

/**
 * API 签名工具类
 * 使用 HMAC-SHA256 算法对请求参数进行签名，确保数据完整性
 * 
 * 签名流程：
 * 1. 客户端将所有请求参数按字母顺序排序
 * 2. 拼接成 key1=value1&key2=value2 格式
 * 3. 使用共享密钥计算 HMAC-SHA256 签名
 * 4. 将签名附加到请求头中
 * 5. 服务端重新计算签名并比对
 */
@Slf4j
public class ApiSignatureUtils {

    private static final String HMAC_SHA256 = "HmacSHA256";

    /**
     * 生成请求签名
     * 
     * @param params 请求参数 Map（会自动排序）
     * @param secret 签名密钥
     * @param timestamp 时间戳（毫秒），用于防止重放攻击
     * @return Base64 编码的签名字符串
     */
    public static String generateSignature(TreeMap<String, String> params, String secret, long timestamp) {
        // 添加时间戳到参数
        params.put("timestamp", String.valueOf(timestamp));
        
        // 构建待签名字符串：key1=value1&key2=value2&...
        StringBuilder sb = new StringBuilder();
        for (var entry : params.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        
        String signString = sb.toString();
        log.debug("待签名字符串: {}", signString);
        
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(keySpec);
            
            byte[] signatureBytes = mac.doFinal(signString.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signatureBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("生成签名失败", e);
            throw new RuntimeException("生成签名失败", e);
        }
    }

    /**
     * 验证请求签名
     * 
     * @param params 请求参数
     * @param secret 签名密钥
     * @param providedSignature 客户端提供的签名
     * @param timestamp 请求时间戳
     * @param toleranceMs 时间戳容差（毫秒），默认 5 分钟
     * @return 签名是否有效
     */
    public static boolean verifySignature(TreeMap<String, String> params, String secret, 
                                          String providedSignature, long timestamp, long toleranceMs) {
        // 验证时间戳是否在允许范围内，防止重放攻击
        long currentTime = System.currentTimeMillis();
        long timeDiff = Math.abs(currentTime - timestamp);
        if (timeDiff > toleranceMs) {
            log.warn("请求时间戳超出允许范围: 差异={}ms, 容差={}ms", timeDiff, toleranceMs);
            return false;
        }
        
        // 重新计算签名
        String expectedSignature = generateSignature(params, secret, timestamp);
        
        // 使用常量时间比较防止时序攻击
        return constantTimeEquals(expectedSignature, providedSignature);
    }

    /**
     * 验证请求签名（使用默认 5 分钟容差）
     */
    public static boolean verifySignature(TreeMap<String, String> params, String secret, 
                                          String providedSignature, long timestamp) {
        return verifySignature(params, secret, providedSignature, timestamp, 5 * 60 * 1000);
    }

    /**
     * 常量时间字符串比较，防止时序攻击
     */
    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        if (a.length() != b.length()) {
            return false;
        }
        
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }

    /**
     * 生成安全的签名密钥（Base64 编码）
     */
    public static String generateSecretKey() {
        byte[] keyBytes = new byte[32]; // 256 bits
        java.security.SecureRandom random = new java.security.SecureRandom();
        random.nextBytes(keyBytes);
        return Base64.getEncoder().encodeToString(keyBytes);
    }
}
