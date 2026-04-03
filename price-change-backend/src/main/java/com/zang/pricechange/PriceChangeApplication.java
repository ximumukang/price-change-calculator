// 指定当前类所在的包名，包名通常采用公司域名倒序 + 项目名
package com.zang.pricechange;

// MyBatis-Plus 注解：扫描指定包下的 Mapper 接口，自动生成实现类
import org.mybatis.spring.annotation.MapperScan;
// Spring Boot 核心类：用于启动应用
import org.springframework.boot.SpringApplication;
// Spring Boot 注解：标记为 Spring Boot 主启动类，自动配置所有组件
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 应用启动类
 * 整个后端的入口，运行 main 方法即启动 Web 服务器
 */
@SpringBootApplication  // 告诉 Spring Boot 这是一个应用，开启自动配置
@MapperScan("com.zang.pricechange.mapper")  // 扫描 mapper 包，让 MyBatis-Plus 知道去哪里找数据库操作接口
public class PriceChangeApplication {
    /**
     * 程序入口方法
     * @param args 命令行参数（一般不用）
     */
    public static void main(String[] args) {
        // 启动 Spring Boot 应用，传入当前类让 Spring 知道配置在哪里
        SpringApplication.run(PriceChangeApplication.class, args);
    }
}
