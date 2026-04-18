package com.zang.pricechange.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

/**
 * 价格项请求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceItemRequest {
    // 分类ID（可选，关联 category 表）
    private Long categoryId;

    // 名称（如"股票A"）
    @NotBlank(message = "名称不能为空")
    @Size(min = 1, max = 100, message = "名称长度不能超过100字符")
    private String name;

    // 当前值
    @NotNull(message = "当前值不能为空")
    @jakarta.validation.constraints.DecimalMin(value = "0.000001", message = "当前值必须大于0")
    private BigDecimal currentValue;

    // 目标值
    @NotNull(message = "目标值不能为空")
    @jakarta.validation.constraints.DecimalMin(value = "0", message = "目标值不能为负数")
    private BigDecimal targetValue;
}
