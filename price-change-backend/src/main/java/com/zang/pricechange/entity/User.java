package com.zang.pricechange.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户实体类 - 对应 users 表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("users")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("username")
    private String username;
    
    @TableField("password_hash")
    private String passwordHash;
    
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
