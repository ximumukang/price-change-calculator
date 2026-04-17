package com.zang.pricechange.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志审计注解
 * 用于标记需要记录操作日志的方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLog {
    /**
     * 操作描述
     */
    String value() default "";

    /**
     * 操作类型
     */
    OperationType operationType() default OperationType.OTHER;

    enum OperationType {
        CREATE("创建"),
        UPDATE("更新"),
        DELETE("删除"),
        QUERY("查询"),
        LOGIN("登录"),
        LOGOUT("登出"),
        OTHER("其他");

        private final String description;

        OperationType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
