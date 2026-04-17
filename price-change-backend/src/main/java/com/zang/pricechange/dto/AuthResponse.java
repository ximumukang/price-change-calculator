// DTO（数据传输对象）所在包
package com.zang.pricechange.dto;

// Lombok 注解
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 认证响应 DTO
 * 
 * 用于登录/注册成功后返回给前端的数据
 * 前端拿到 token 后，后续请求都携带这个 token 证明身份
 */
@Data  // Lombok：自动生成 getter、setter、toString、equals、hashCode
@NoArgsConstructor  // Lombok：生成无参构造方法
@AllArgsConstructor  // Lombok：生成包含所有字段的构造方法
public class AuthResponse {
    // JWT Access Token：用户身份凭证，后续请求需要携带
    private String token;
    // Refresh Token：用于刷新 Access Token，过期时间更长
    private String refreshToken;
    // 用户名：用于前端显示
    private String username;
}
