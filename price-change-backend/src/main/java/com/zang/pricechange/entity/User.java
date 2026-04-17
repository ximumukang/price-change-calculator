package com.zang.pricechange.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.apache.ibatis.type.StringTypeHandler;

import java.time.LocalDateTime;

/**
 * 用户实体类 - 对应 users 表
 * 用于存储用户基本信息，包括用户名、密码哈希和创建时间
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "users", autoResultMap = true)
public class User {
    /**
     * 用户ID，主键，雪花算法生成
      */
     @TableId(type = IdType.ASSIGN_ID)
     private Long id;
    
    /**
     * 用户名，唯一标识用户
     * 明确指定使用 StringTypeHandler，避免被 EncryptedStringTypeHandler 加密
     */
    @TableField(value = "username", typeHandler = StringTypeHandler.class)
    private String username;
    
    /**
     * 密码哈希值，使用BCrypt算法加密存储
     * 不直接存储明文密码，保证安全性
     * 明确指定使用 StringTypeHandler，避免被 EncryptedStringTypeHandler 加密
     */
    @TableField(value = "password_hash", typeHandler = StringTypeHandler.class)
    private String passwordHash;
    
    /**
     * 创建时间，插入记录时自动填充当前时间
     * 由MyBatis-Plus的自动填充功能管理
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
