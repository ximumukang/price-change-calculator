package com.zang.pricechange.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zang.pricechange.security.EncryptedStringTypeHandler;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 价格项实体类 - 对应 price_item 表
 * 
 * 安全特性：
 * - name 字段使用 AES-256-GCM 加密存储
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "price_item", autoResultMap = true)
public class PriceItem {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    @TableField("user_id")
    private Long userId;

    /**
     * 分类ID（外键关联 category 表）
     */
    @TableField("category_id")
    private Long categoryId;
    
    /**
     * 价格项名称（加密存储）
     * 使用 AES-256-GCM 加密防止数据库泄露时暴露敏感信息
     */
    @TableField(value = "name", typeHandler = EncryptedStringTypeHandler.class)
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
