package com.zang.pricechange.controller;

import com.zang.pricechange.common.Result;
import com.zang.pricechange.dto.AuthResponse;
import com.zang.pricechange.dto.LoginRequest;
import com.zang.pricechange.dto.PublicKeyResponse;
import com.zang.pricechange.dto.RegisterRequest;
import com.zang.pricechange.entity.User;
import com.zang.pricechange.security.JwtTokenProvider;
import com.zang.pricechange.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 处理登录、注册、获取 RSA 公钥等接口
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 获取 RSA 公钥接口
     */
    @GetMapping("/public-key")
    public Result<PublicKeyResponse> getPublicKey() {
        return Result.success(new PublicKeyResponse(userService.getPublicKey()));
    }

    /**
     * 用户注册接口
     */
    @PostMapping("/register")
    public Result<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.register(request.getUsername(), request.getPassword());
        return Result.success(buildAuthResponse(user));
    }

    /**
     * 用户登录接口
     */
    @PostMapping("/login")
    public Result<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = userService.login(request.getUsername(), request.getPassword());
        return Result.success(buildAuthResponse(user));
    }

    private AuthResponse buildAuthResponse(User user) {
        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getId());
        return new AuthResponse(token, user.getUsername());
    }
}
