package com.zang.pricechange.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 价格项响应 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceItemResponse {
    // 记录 ID
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    // 分类ID
    @JsonSerialize(using = ToStringSerializer.class)
    private Long categoryId;
    // 分类名称（联查返回，方便前端显示）
    private String categoryName;
    // 名称（如"股票A"、"比特币"）
    private String name;
    // 当前值
    private BigDecimal currentValue;
    // 目标值
    private BigDecimal targetValue;
    // 涨跌幅（百分比）
    private BigDecimal changePercent;
    // 创建时间
    private LocalDateTime createdAt;
}
