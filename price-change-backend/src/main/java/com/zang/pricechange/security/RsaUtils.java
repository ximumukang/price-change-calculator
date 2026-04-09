package com.zang.pricechange.security;

import lombok.Getter;
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
 * 使用 PKCS1Padding 填充模式
 * 
 * <p>功能说明：</p>
 * <ul>
 *   <li>应用启动时自动生成 2048 位 RSA 密钥对</li>
 *   <li>提供公钥字符串用于前端加密传输敏感数据（如密码）</li>
 *   <li>使用私钥解密前端传来的加密数据</li>
 * </ul>
 * 
 * <p>安全说明：</p>
 * <ul>
 *   <li>采用 RSA/ECB/PKCS1Padding 转换模式，与前端 jsencrypt 库兼容</li>
 *   <li>密钥对在内存中保存，应用重启后重新生成</li>
 *   <li>适用于单机部署场景，集群部署需使用统一的密钥对</li>
 * </ul>
 */
@Component
public class RsaUtils {

    /**
     * RSA 转换模式：RSA/ECB/PKCS1Padding
     * - RSA: 非对称加密算法
     * - ECB: 电子密码本模式（RSA 不使用块链接，固定为此模式）
     * - PKCS1Padding: PKCS#1 v1.5 填充方案，与前端 jsencrypt 库兼容
     */
    private static final String RSA_TRANSFORMATION = "RSA/ECB/PKCS1Padding";

    /**
     * RSA 私钥，用于解密前端传来的加密数据
     */
    @Getter
    private final PrivateKey privateKey;
    
    /**
     * RSA 公钥，用于提供给前端进行加密
     */
    @Getter
    private final PublicKey publicKey;

    /**
     * 构造函数：在组件初始化时生成 RSA 密钥对
     * 
     * <p>密钥对生成流程：</p>
     * <ol>
     *   <li>创建 KeyPairGenerator 实例，指定 RSA 算法</li>
     *   <li>初始化密钥长度为 2048 位（推荐的安全长度）</li>
     *   <li>生成密钥对并提取公钥和私钥</li>
     * </ol>
     * 
     * @throws RuntimeException 如果密钥对生成失败（极少发生）
     */
    public RsaUtils() {
        try {
            // 获取 RSA 密钥对生成器实例
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            // 初始化密钥长度为 2048 位
            generator.initialize(2048);
            // 生成密钥对
            KeyPair keyPair = generator.generateKeyPair();
            // 提取私钥和公钥
            this.privateKey = keyPair.getPrivate();
            this.publicKey = keyPair.getPublic();
        } catch (Exception e) {
            throw new RuntimeException("RSA 密钥对生成失败", e);
        }
    }

    /**
     * 获取公钥的 Base64 编码字符串
     * 
     * <p>用途：提供给前端用于加密敏感数据（如登录密码）</p>
     * <p>格式：Base64 编码的 DER 格式公钥</p>
     * 
     * @return Base64 编码的公钥字符串
     */
    public String getPublicKeyString() {
        return Base64.getEncoder().encodeToString(getPublicKey().getEncoded());
    }

    /**
     * 使用私钥解密数据（PKCS1Padding 填充）
     * 
     * <p>解密流程：</p>
     * <ol>
     *   <li>将 Base64 编码的密文解码为字节数组</li>
     *   <li>使用 RSA 私钥和 PKCS1Padding 模式初始化解密器</li>
     *   <li>执行解密操作得到原始明文字节</li>
     *   <li>将字节数组转换为 UTF-8 字符串</li>
     * </ol>
     * 
     * <p>典型应用场景：</p>
     * <ul>
     *   <li>解密前端传来的登录密码</li>
     *   <li>解密其他需要安全传输的敏感信息</li>
     * </ul>
     * 
     * @param encrypted Base64 编码的 RSA 加密数据
     * @return 解密后的原始字符串
     * @throws RuntimeException 如果解密失败（密文损坏、密钥不匹配等）
     */
    public String decrypt(String encrypted) {
        try {
            // 创建 Cipher 实例，指定 RSA/ECB/PKCS1Padding 转换模式
            Cipher cipher = Cipher.getInstance(RSA_TRANSFORMATION);
            // 初始化为解密模式，使用私钥
            cipher.init(Cipher.DECRYPT_MODE, getPrivateKey());
            // Base64 解码并执行解密操作
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encrypted));
            // 将解密后的字节转换为 UTF-8 字符串
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("RSA 解密失败", e);
        }
    }
}
