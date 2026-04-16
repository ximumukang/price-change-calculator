package com.zang.pricechange.security;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * RSA 加密/解密工具类
 * 使用 PKCS1Padding 填充模式
 *
 * <p>功能说明：</p>
 * <ul>
 *   <li>从配置文件加载固定的 2048 位 RSA 密钥对</li>
 *   <li>提供公钥字符串用于前端加密传输敏感数据（如密码）</li>
 *   <li>使用私钥解密前端传来的加密数据</li>
 * </ul>
 *
 * <p>安全说明：</p>
 * <ul>
 *   <li>采用 RSA/ECB/PKCS1Padding 转换模式，与前端 jsencrypt 库兼容</li>
 *   <li>密钥对从 application.yml 配置文件加载，应用重启后密钥保持不变</li>
 *   <li>适用于单机和集群部署场景</li>
 * </ul>
 */
@Slf4j
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
     * 构造函数：从配置文件加载 RSA 密钥对
     *
     * <p>密钥加载流程：</p>
     * <ol>
     *   <li>从 application.yml 读取 Base64 编码的私钥和公钥</li>
     *   <li>使用 KeyFactory 将 Base64 解码后的字节还原为密钥对象</li>
     *   <li>私钥使用 PKCS8EncodedKeySpec（PKCS#8 格式）</li>
     *   <li>公钥使用 X509EncodedKeySpec（X.509 格式）</li>
     * </ol>
     *
     * @param privateKeyBase64 Base64 编码的 PKCS#8 格式私钥
     * @param publicKeyBase64  Base64 编码的 X.509 格式公钥
     * @throws RuntimeException 如果密钥加载失败（格式错误、密钥不匹配等）
     */
    public RsaUtils(
            @Value("${app.rsa.private-key}") String privateKeyBase64,
            @Value("${app.rsa.public-key}") String publicKeyBase64) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            // 加载私钥：PKCS#8 格式
            byte[] privateBytes = Base64.getDecoder().decode(privateKeyBase64);
            PKCS8EncodedKeySpec privateSpec = new PKCS8EncodedKeySpec(privateBytes);
            this.privateKey = keyFactory.generatePrivate(privateSpec);

            // 加载公钥：X.509 格式
            byte[] publicBytes = Base64.getDecoder().decode(publicKeyBase64);
            X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(publicBytes);
            this.publicKey = keyFactory.generatePublic(publicSpec);

            log.info("RSA 密钥对从配置文件加载成功");
        } catch (Exception e) {
            throw new RuntimeException("RSA 密钥对加载失败，请检查 app.rsa.private-key 和 app.rsa.public-key 配置", e);
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
