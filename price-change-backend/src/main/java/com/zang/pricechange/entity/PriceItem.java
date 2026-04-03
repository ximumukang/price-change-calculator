package com.zang.pricechange.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 价格项实体类 - 对应 price_item 表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("price_item")
public class PriceItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("user_id")
    private Long userId;
    
    @TableField("name")
    private String name;
    
    @TableField("current_value")
    private BigDecimal currentValue;
    
    @TableField("target_value")
    private BigDecimal targetValue;
    
    @TableField("change_percent")
    private BigDecimal changePercent;
    
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
