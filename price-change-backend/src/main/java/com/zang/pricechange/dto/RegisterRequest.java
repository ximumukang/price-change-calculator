// DTO（数据传输对象）所在包
package com.zang.pricechange.dto;

// 参数校验注解
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
// Lombok 注解
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 注册请求 DTO
 * 
 * 用于接收前端注册时发送的用户名和密码
 * 数据已经过 RSA 加密，Service 层会先解密再处理
 */
@Data  // Lombok：自动生成 getter、setter、toString、equals、hashCode
@NoArgsConstructor  // Lombok：生成无参构造方法
@AllArgsConstructor  // Lombok：生成包含所有字段的构造方法
public class RegisterRequest {
    // 加密后的用户名
    @NotBlank(message = "用户名不能为空")  // 校验：不能为空
    @Size(min = 3, max = 500, message = "用户名长度3-500字符")  // 校验：长度限制
    private String username;

    // 加密后的密码
    @NotBlank(message = "密码不能为空")  // 校验：不能为空
    @Size(min = 6, max = 500, message = "密码长度6-500字符")  // 校验：长度限制（至少6位）
    private String password;
}
