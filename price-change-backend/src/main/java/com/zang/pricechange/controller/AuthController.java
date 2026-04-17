package com.zang.pricechange.controller;

import com.zang.pricechange.annotation.AuditLog;
import com.zang.pricechange.common.Result;
import com.zang.pricechange.dto.AuthResponse;
import com.zang.pricechange.dto.LoginRequest;
import com.zang.pricechange.dto.PublicKeyResponse;
import com.zang.pricechange.dto.RefreshTokenRequest;
import com.zang.pricechange.dto.RegisterRequest;
import com.zang.pricechange.entity.User;
import com.zang.pricechange.security.JwtTokenProvider;
import com.zang.pricechange.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 处理登录、注册、获取 RSA 公钥、刷新 Token 等接口
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户登录、注册、获取公钥、刷新 Token 接口")
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 获取 RSA 公钥接口
     */
    @Operation(summary = "获取RSA公钥", description = "获取用于前端加密密码的RSA公钥")
    @GetMapping("/public-key")
    public Result<PublicKeyResponse> getPublicKey() {
        return Result.success(new PublicKeyResponse(userService.getPublicKey()));
    }

    /**
     * 用户注册接口
     */
    @AuditLog(value = "用户注册", operationType = AuditLog.OperationType.CREATE)
    @Operation(summary = "用户注册", description = "注册新用户，自动返回JWT Token")
    @PostMapping("/register")
    public Result<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.register(request.getUsername(), request.getPassword());
        return Result.success(buildAuthResponse(user));
    }

    /**
     * 用户登录接口
     */
    @AuditLog(value = "用户登录", operationType = AuditLog.OperationType.LOGIN)
    @Operation(summary = "用户登录", description = "用户名密码登录，返回JWT Token")
    @PostMapping("/login")
    public Result<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = userService.login(request.getUsername(), request.getPassword());
        return Result.success(buildAuthResponse(user));
    }

    /**
     * 刷新 Token 接口
     */
    @AuditLog(value = "刷新Token", operationType = AuditLog.OperationType.OTHER)
    @Operation(summary = "刷新Token", description = "使用 Refresh Token 获取新的 Access Token")
    @PostMapping("/refresh")
    public Result<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        // 验证 Refresh Token 有效性
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Refresh Token 无效或已过期");
        }

        // 从 Refresh Token 中解析用户信息
        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);

        // 生成新的 Access Token 和 Refresh Token
        String newAccessToken = jwtTokenProvider.generateToken(username, userId);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(username, userId);

        return Result.success(new AuthResponse(newAccessToken, newRefreshToken, username));
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtTokenProvider.generateToken(user.getUsername(), user.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername(), user.getId());
        return new AuthResponse(accessToken, refreshToken, user.getUsername());
    }
}
