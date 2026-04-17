package com.zang.pricechange.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zang.pricechange.entity.User;
import com.zang.pricechange.mapper.UserMapper;
import com.zang.pricechange.security.RsaUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户服务类
 * 处理用户相关的业务逻辑：注册、登录、获取公钥
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RsaUtils rsaUtils;
    private final AccountLockService accountLockService;

    /**
     * 获取 RSA 公钥
     */
    public String getPublicKey() {
        return rsaUtils.getPublicKeyString();
    }

    /**
     * 用户注册
     */
    public User register(String encryptedUsername, String encryptedPassword) {
        String[] decrypted = decryptCredentials(encryptedUsername, encryptedPassword);
        String username = decrypted[0];
        String password = decrypted[1];

        // 调试日志：打印解密后的用户名和密码长度
        log.info("[DEBUG] 注册 - 解密后用户名: [{}], 长度: {}, 密码长度: {}", 
            username, username != null ? username.length() : 0, password != null ? password.length() : 0);

        // 验证用户名格式
        validateUsernameFormat(username);

        // 验证密码复杂度
        validatePasswordComplexity(password);

        // 检查用户名是否已存在（注意：这里返回具体错误信息，生产环境可考虑统一提示）
        if (findByUsername(username) != null) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        
        // 调试日志：打印即将存入数据库的用户名
        log.info("[DEBUG] 注册 - 即将存入数据库的用户名: [{}]", user.getUsername());
        
        userMapper.insert(user);
        return user;
    }

    /**
     * 验证用户名格式
     */
    private void validateUsernameFormat(String username) {
        if (username == null || username.length() < 3 || username.length() > 50) {
            throw new IllegalArgumentException("用户名长度必须在3-50字符之间");
        }
        // 只允许字母、数字、下划线、中文字符
        if (!username.matches("^[a-zA-Z0-9_\\u4e00-\\u9fa5]+$")) {
            throw new IllegalArgumentException("用户名只能包含字母、数字、下划线和中文字符");
        }
    }

    private void validatePasswordComplexity(String password) {
        if (password.length() < 8) {
            throw new IllegalArgumentException("密码长度至少8位");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new IllegalArgumentException("密码必须包含小写字母");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("密码必须包含大写字母");
        }
        if (!password.matches(".*\\d.*")) {
            throw new IllegalArgumentException("密码必须包含数字");
        }
    }

    /**
     * 用户登录
     */
    public User login(String encryptedUsername, String encryptedPassword) {
        String[] decrypted = decryptCredentials(encryptedUsername, encryptedPassword);
        String username = decrypted[0];
        String password = decrypted[1];

        // 检查账户是否被锁定
        if (accountLockService.isLocked(username)) {
            long remainingSeconds = accountLockService.getRemainingLockSeconds(username);
            throw new RuntimeException(String.format("账户已被锁定，请 %d 秒后重试", remainingSeconds));
        }

        User user = findByUsername(username);
        if (user == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            // 记录失败次数
            accountLockService.recordFailure(username);
            throw new RuntimeException("用户名或密码错误");
        }

        // 登录成功，清除失败记录
        accountLockService.clearFailures(username);
        return user;
    }

    /**
     * 根据用户名查询用户
     */
    private User findByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return userMapper.selectOne(wrapper);
    }

    /**
     * 解密用户名和密码
     */
    private String[] decryptCredentials(String encryptedUsername, String encryptedPassword) {
        try {
            // 记录接收到的加密数据长度
            log.info("接收到加密数据 - 用户名密文长度: {}, 密码密文长度: {}", 
                encryptedUsername != null ? encryptedUsername.length() : 0,
                encryptedPassword != null ? encryptedPassword.length() : 0);
            
            String decryptedUsername = rsaUtils.decrypt(encryptedUsername);
            String decryptedPassword = rsaUtils.decrypt(encryptedPassword);
            
            // 调试日志：记录解密后的内容（仅用于调试）
            log.info("解密完成 - 用户名: [{}], 密码长度: {}", 
                decryptedUsername,
                decryptedPassword != null ? decryptedPassword.length() : 0);
            
            return new String[]{decryptedUsername, decryptedPassword};
        } catch (Exception e) {
            log.error("凭据解密失败", e);
            throw new RuntimeException("密码解密失败，请重试");
        }
    }
}
