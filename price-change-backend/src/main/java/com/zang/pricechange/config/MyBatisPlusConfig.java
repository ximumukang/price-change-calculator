// 配置类所在的包
package com.zang.pricechange.config;

// MyBatis-Plus 数据库类型枚举
import com.baomidou.mybatisplus.annotation.DbType;
// MyBatis-Plus 元数据处理器接口：用于自动填充字段（如创建时间）
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
// MyBatis-Plus 拦截器：用于添加分页等功能
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
// MyBatis-Plus 分页插件
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
// MyBatis 元数据对象：用于读写实体类属性
import org.apache.ibatis.reflection.MetaObject;
// Spring 注解：声明这是一个配置类，Spring 会自动管理其中的 Bean
import org.springframework.context.annotation.Bean;
// Spring 注解：标记为配置类
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 配置类
 * 负责配置分页插件和字段自动填充功能
 */
@Configuration  // 告诉 Spring 这是一个配置类，里面的 @Bean 方法会创建对象交给 Spring 管理
public class MyBatisPlusConfig implements MetaObjectHandler {

    /**
     * 配置 MyBatis-Plus 拦截器
     * 这里添加了分页插件，这样后续查询就可以使用分页功能
     * @return 配置好的拦截器
     */
    @Bean  // 告诉 Spring：调用这个方法创建一个对象，并放入容器中管理
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 添加 MySQL 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    /**
     * 插入数据时自动填充字段
     * 当调用 mapper.insert() 时，会自动把 createdAt 设为当前时间
     * @param metaObject 元数据对象，可以读取和设置实体类的属性
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        // 严格填充：只有当 createdAt 为 null 时才填充
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
    }

    /**
     * 更新数据时自动填充字段
     * 目前为空，如果以后需要自动更新 updateTime 可以在这里添加
     * @param metaObject 元数据对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        // 暂时不需要自动填充更新字段
    }
}
