package com.zang.pricechange.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 速率限制配置类
 * 使用 Bucket4j 实现基于令牌桶算法的限流
 */
@Configuration
@Getter
public class RateLimitConfig {

    /**
     * 登录接口限流：每分钟允许的请求数
     */
    @Value("${rate.limit.login.per-minute:5}")
    private int loginRequestsPerMinute;

    /**
     * 通用 API 限流：每分钟允许的请求数
     */
    @Value("${rate.limit.api.per-minute:60}")
    private int apiRequestsPerMinute;

    /**
     * 存储每个 IP 地址对应的速率限制桶
     * Key: IP 地址
     * Value: Bucket 实例
     */
    private final Map<String, Bucket> loginBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> apiBuckets = new ConcurrentHashMap<>();

    /**
     * 获取或创建登录接口的限流桶
     *
     * @param ip 客户端 IP 地址
     * @return Bucket 实例
     */
    public Bucket resolveLoginBucket(String ip) {
        return loginBuckets.computeIfAbsent(ip, this::newLoginBucket);
    }

    /**
     * 获取或创建通用 API 的限流桶
     *
     * @param ip 客户端 IP 地址
     * @return Bucket 实例
     */
    public Bucket resolveApiBucket(String ip) {
        return apiBuckets.computeIfAbsent(ip, this::newApiBucket);
    }

    /**
     * 创建新的登录限流桶
     *
     * @param ip 客户端 IP 地址
     * @return 配置好的 Bucket 实例
     */
    private Bucket newLoginBucket(String ip) {
        Bandwidth limit = Bandwidth.classic(loginRequestsPerMinute, Refill.greedy(loginRequestsPerMinute, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    /**
     * 创建新的 API 限流桶
     *
     * @param ip 客户端 IP 地址
     * @return 配置好的 Bucket 实例
     */
    private Bucket newApiBucket(String ip) {
        Bandwidth limit = Bandwidth.classic(apiRequestsPerMinute, Refill.greedy(apiRequestsPerMinute, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }
}
