// DTO（数据传输对象）所在包
package com.zang.pricechange.dto;

// Lombok 注解
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RSA 公钥响应 DTO
 * 
 * 用于返回 RSA 公钥给前端
 * 前端获取公钥后，用公钥加密密码再传输，保证安全性
 */
@Data  // Lombok：自动生成 getter、setter、toString、equals、hashCode
@NoArgsConstructor  // Lombok：生成无参构造方法
@AllArgsConstructor  // Lombok：生成包含所有字段的构造方法
public class PublicKeyResponse {
    // Base64 编码的 RSA 公钥字符串
    private String publicKey;
}
