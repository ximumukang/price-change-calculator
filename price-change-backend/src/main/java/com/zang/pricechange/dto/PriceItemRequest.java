// DTO（数据传输对象）所在包
package com.zang.pricechange.dto;

// 参数校验注解
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
// Lombok 注解
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

/**
 * 价格项请求 DTO
 * 
 * 用于接收前端创建涨跌幅记录时发送的数据
 * 包含校验注解，确保数据合法性
 */
@Data  // Lombok：自动生成 getter、setter、toString、equals、hashCode
@NoArgsConstructor  // Lombok：生成无参构造方法
@AllArgsConstructor  // Lombok：生成包含所有字段的构造方法
public class PriceItemRequest {
    // 名称（如"股票A"）
    @NotBlank(message = "名称不能为空")  // 校验：不能为空字符串或 null
    @jakarta.validation.constraints.Size(min = 1, max = 100, message = "名称长度不能超过100字符")  // 校验：长度限制
    private String name;

    // 当前值
    @NotNull(message = "当前值不能为空")  // 校验：不能为 null
    @jakarta.validation.constraints.DecimalMin(value = "0.000001", message = "当前值必须大于0")  // 校验：最小值
    private BigDecimal currentValue;

    // 目标值
    @NotNull(message = "目标值不能为空")  // 校验：不能为 null
    @jakarta.validation.constraints.DecimalMin(value = "0", message = "目标值不能为负数")  // 校验：最小值
    private BigDecimal targetValue;
}
