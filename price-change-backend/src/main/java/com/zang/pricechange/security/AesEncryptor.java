package com.zang.pricechange.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES 加密工具类
 * 使用 AES-256-GCM 模式提供安全的对称加密
 * 
 * 安全特性：
 * - 使用 GCM 模式提供认证加密（AEAD）
 * - 每次加密使用随机 IV（12字节）
 * - 密钥从环境变量读取
 */
@Slf4j
@Component
public class AesEncryptor {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int KEY_SIZE = 256; // bits
    private static final int IV_LENGTH = 12; // bytes
    private static final int TAG_LENGTH = 128; // bits

    private final SecretKey secretKey;

    /**
     * 构造函数：从环境变量读取加密密钥
     * 
     * <p>安全说明：</p>
     * <ul>
     *   <li>生产环境必须设置 ENCRYPTION_KEY 环境变量</li>
     *   <li>开发环境可使用 generateKey() 方法生成随机密钥</li>
     *   <li>密钥变更会导致之前加密的数据无法解密，请妥善保管</li>
     * </ul>
     */
    public AesEncryptor() {
        String keyBase64 = System.getenv("ENCRYPTION_KEY");
        if (keyBase64 == null || keyBase64.isEmpty()) {
            // ⚠️ 生产环境禁止使用默认密钥！
            log.error("========================================");
            log.error("严重警告：ENCRYPTION_KEY 环境变量未设置！");
            log.error("当前使用弱密钥，仅用于开发测试环境。");
            log.error("生产环境必须设置强密钥：");
            log.error("  1. 运行: java -cp app.jar com.zang.pricechange.security.AesEncryptor --generate-key");
            log.error("  2. 或使用工具生成: openssl rand -base64 32");
            log.error("  3. 设置环境变量: export ENCRYPTION_KEY=<生成的密钥>");
            log.error("========================================");
            keyBase64 = generateDefaultKey();
        }
        this.secretKey = decodeKey(keyBase64);
        log.info("AES 加密密钥加载成功");
    }

    /**
     * 加密字符串
     * 
     * @param plaintext 明文
     * @return Base64 编码的密文（格式：IV + 密文）
     */
    public String encrypt(String plaintext) {
        if (plaintext == null || plaintext.isEmpty()) {
            return plaintext;
        }
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            byte[] iv = new byte[IV_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
            
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
            
            byte[] plaintextBytes = plaintext.getBytes(StandardCharsets.UTF_8);
            byte[] ciphertext = cipher.doFinal(plaintextBytes);
            
            // 将 IV 和密文合并：IV (12字节) + 密文
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + ciphertext.length);
            byteBuffer.put(iv);
            byteBuffer.put(ciphertext);
            
            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception e) {
            log.error("加密失败", e);
            throw new RuntimeException("加密失败", e);
        }
    }

    /**
     * 解密字符串
     * 
     * @param ciphertext Base64 编码的密文（格式：IV + 密文）
     * @return 解密后的明文
     */
    public String decrypt(String ciphertext) {
        if (ciphertext == null || ciphertext.isEmpty()) {
            return ciphertext;
        }
        try {
            byte[] combined = Base64.getDecoder().decode(ciphertext);
            
            // 提取 IV
            byte[] iv = new byte[IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, iv.length);
            
            // 提取密文
            byte[] ciphertextBytes = new byte[combined.length - IV_LENGTH];
            System.arraycopy(combined, IV_LENGTH, ciphertextBytes, 0, ciphertextBytes.length);
            
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
            
            byte[] plaintextBytes = cipher.doFinal(ciphertextBytes);
            return new String(plaintextBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("解密失败", e);
            throw new RuntimeException("解密失败", e);
        }
    }

    /**
     * 生成新的加密密钥（Base64 编码）
     * 用于初始化时生成密钥
     */
    public static String generateKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(KEY_SIZE, new SecureRandom());
            SecretKey key = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(key.getEncoded());
        } catch (Exception e) {
            log.error("生成密钥失败", e);
            throw new RuntimeException("生成密钥失败", e);
        }
    }

    /**
     * 从 Base64 字符串解码密钥
     */
    private SecretKey decodeKey(String base64Key) {
        byte[] decodedKey = Base64.getDecoder().decode(base64Key);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGORITHM);
    }

    /**
     * 生成默认密钥（仅用于开发环境）
     * ⚠️ 生产环境禁止使用此方法！
     */
    private String generateDefaultKey() {
        // 固定的默认密钥，仅用于开发/测试环境
        // 警告：这是一个弱密钥，生产环境必须使用强随机密钥
        return "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";
    }

    /**
     * 主方法：生成并打印一个新的加密密钥
     * 使用方法：java -cp app.jar com.zang.pricechange.security.AesEncryptor --generate-key
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        if (args.length > 0 && "--generate-key".equals(args[0])) {
            String key = generateKey();
            System.out.println("========================================");
            System.out.println("生成的 AES-256 加密密钥：");
            System.out.println(key);
            System.out.println("========================================");
            System.out.println("请将此密钥设置为环境变量 ENCRYPTION_KEY");
            System.out.println("Windows: setx ENCRYPTION_KEY \"" + key + "\"");
            System.out.println("Linux/Mac: export ENCRYPTION_KEY=\"" + key + "\"");
            System.out.println("========================================");
        } else {
            System.out.println("用法: java -cp app.jar com.zang.pricechange.security.AesEncryptor --generate-key");
        }
    }
}
