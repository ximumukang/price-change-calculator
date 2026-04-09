package com.zang.pricechange.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zang.pricechange.entity.User;
import com.zang.pricechange.mapper.UserMapper;
import com.zang.pricechange.security.RsaUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户服务类
 * 处理用户相关的业务逻辑：注册、登录、获取公钥
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RsaUtils rsaUtils;

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

        if (findByUsername(username) != null) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        userMapper.insert(user);
        return user;
    }

    /**
     * 用户登录
     */
    public User login(String encryptedUsername, String encryptedPassword) {
        String[] decrypted = decryptCredentials(encryptedUsername, encryptedPassword);
        String username = decrypted[0];
        String password = decrypted[1];

        User user = findByUsername(username);
        if (user == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("用户名或密码错误");
        }
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
        return new String[]{
                rsaUtils.decrypt(encryptedUsername),
                rsaUtils.decrypt(encryptedPassword)
        };
    }
}
