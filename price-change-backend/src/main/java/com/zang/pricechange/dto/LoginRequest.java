// DTO（数据传输对象）所在包
package com.zang.pricechange.dto;

// 参数校验注解
import jakarta.validation.constraints.NotBlank;
// Lombok 注解
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 登录请求 DTO
 * 
 * 用于接收前端登录时发送的用户名和密码
 * 数据已经过 RSA 加密，Service 层会先解密再验证
 */
@Data  // Lombok：自动生成 getter、setter、toString、equals、hashCode
@NoArgsConstructor  // Lombok：生成无参构造方法
@AllArgsConstructor  // Lombok：生成包含所有字段的构造方法
public class LoginRequest {
    // 加密后的用户名
    @NotBlank(message = "用户名不能为空")  // 校验：不能为空
    private String username;

    // 加密后的密码
    @NotBlank(message = "密码不能为空")  // 校验：不能为空
    private String password;
}
