package com.zang.pricechange.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 账户锁定服务
 * 记录登录失败次数，连续失败多次后临时锁定账户
 */
@Slf4j
@Service
public class AccountLockService {

    /**
     * 最大失败次数
     */
    @Value("${account.lock.max-failures:5}")
    private int maxFailures;

    /**
     * 锁定时长（分钟）
     */
    @Value("${account.lock.duration-minutes:15}")
    private int lockDurationMinutes;

    /**
     * 存储每个用户的失败尝试信息
     * Key: 用户名
     * Value: 失败记录
     */
    private final Map<String, LoginFailureRecord> failureRecords = new ConcurrentHashMap<>();

    /**
     * 记录登录失败
     *
     * @param username 用户名
     */
    public void recordFailure(String username) {
        LoginFailureRecord record = failureRecords.computeIfAbsent(username, k -> new LoginFailureRecord());
        record.incrementFailures();
        log.warn("用户 {} 登录失败，当前失败次数: {}", username, record.getFailures());

        if (record.getFailures() >= maxFailures) {
            record.lockUntil(Instant.now().plus(Duration.ofMinutes(lockDurationMinutes)));
            log.warn("用户 {} 因连续失败 {} 次被锁定 {} 分钟", username, maxFailures, lockDurationMinutes);
        }
    }

    /**
     * 清除登录失败记录（登录成功时调用）
     *
     * @param username 用户名
     */
    public void clearFailures(String username) {
        failureRecords.remove(username);
        log.debug("用户 {} 登录成功，清除失败记录", username);
    }

    /**
     * 检查账户是否被锁定
     *
     * @param username 用户名
     * @return true 表示已锁定，false 表示未锁定
     */
    public boolean isLocked(String username) {
        LoginFailureRecord record = failureRecords.get(username);
        if (record == null) {
            return false;
        }

        // 检查是否超过锁定时长
        if (record.isLocked()) {
            if (Instant.now().isAfter(record.getLockUntil())) {
                // 锁定已过期，清除记录
                failureRecords.remove(username);
                log.debug("用户 {} 锁定已过期，自动解锁", username);
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * 获取剩余锁定时间（秒）
     *
     * @param username 用户名
     * @return 剩余秒数，0 表示未锁定
     */
    public long getRemainingLockSeconds(String username) {
        LoginFailureRecord record = failureRecords.get(username);
        if (record == null || !record.isLocked()) {
            return 0;
        }

        long remaining = Duration.between(Instant.now(), record.getLockUntil()).getSeconds();
        return Math.max(0, remaining);
    }

    /**
     * 登录失败记录
     */
    private static class LoginFailureRecord {
        private int failures = 0;
        private Instant lockUntil = null;

        public void incrementFailures() {
            this.failures++;
        }

        public int getFailures() {
            return failures;
        }

        public void lockUntil(Instant until) {
            this.lockUntil = until;
        }

        public boolean isLocked() {
            return lockUntil != null;
        }

        public Instant getLockUntil() {
            return lockUntil;
        }
    }
}
