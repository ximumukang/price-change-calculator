// DTO（数据传输对象）所在包
package com.zang.pricechange.dto;

// Lombok 注解
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 价格项响应 DTO
 * 
 * DTO（Data Transfer Object）用于接口返回数据的格式
 * 与 Entity（实体类）的区别：
 * - Entity 对应数据库表结构
 * - DTO 是前端需要的数据格式
 * 
 * 这个类用于返回涨跌幅记录给前端
 * 不包含 userId 等敏感信息，只返回前端需要展示的数据
 */
@Data  // Lombok：自动生成 getter、setter、toString、equals、hashCode
@NoArgsConstructor  // Lombok：生成无参构造方法
@AllArgsConstructor  // Lombok：生成包含所有字段的构造方法
public class PriceItemResponse {
    // 记录 ID
    private Long id;
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
