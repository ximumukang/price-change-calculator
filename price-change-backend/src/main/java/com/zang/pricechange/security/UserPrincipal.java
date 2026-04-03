// 安全相关类所在包
package com.zang.pricechange.security;

// Lombok 注解：自动生成 getter 方法
import lombok.Getter;
// Lombok 注解：自动生成包含所有 final 字段的构造器
import lombok.RequiredArgsConstructor;

/**
 * 当前登录用户信息
 * 这个类用于在 Spring Security 中存储当前登录用户的信息
 * 在 Controller 中通过 @AuthenticationPrincipal 注解获取
 */
@Getter  // 自动生成所有字段的 getter 方法（如 getUserId()、getUsername()）
@RequiredArgsConstructor  // 自动生成包含所有 final 字段的构造器
public class UserPrincipal {
    // 用户 ID（数据库主键）
    private final Long userId;
    // 用户名
    private final String username;
}
