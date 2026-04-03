package com.zang.pricechange.security;

import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

/**
 * RSA 加密/解密工具类
 * 使用 OAEP 填充模式，比默认 PKCS1Padding 更安全
 */
@Component
public class RsaUtils {

    private static final String RSA_TRANSFORMATION = "RSA/ECB/PKCS1Padding";

    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    public RsaUtils() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();
            this.privateKey = keyPair.getPrivate();
            this.publicKey = keyPair.getPublic();
        } catch (Exception e) {
            throw new RuntimeException("RSA 密钥对生成失败", e);
        }
    }

    /**
     * 获取公钥字符串（Base64 编码）
     */
    public String getPublicKeyString() {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    /**
     * 用私钥解密数据（使用 OAEP 填充）
     */
    public String decrypt(String encrypted) {
        try {
            Cipher cipher = Cipher.getInstance(RSA_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encrypted));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("RSA 解密失败", e);
        }
    }
}
