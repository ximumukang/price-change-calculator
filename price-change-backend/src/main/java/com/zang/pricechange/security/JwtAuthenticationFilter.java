package com.zang.pricechange.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT 认证过滤器
 * 从请求头提取 Token，验证后设置用户信息到 SecurityContext
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 执行 JWT 认证过滤逻辑
     * <p>
     * 1. 检查 SecurityContext 是否已有认证信息，避免重复处理
     * 2. 从请求头提取 Bearer Token
     * 3. 验证 Token 有效性
     * 4. 解析用户信息并设置到 SecurityContext
     *
     * @param request      HTTP 请求对象
     * @param response     HTTP 响应对象
     * @param filterChain  过滤器链
     * @throws ServletException Servlet 异常
     * @throws IOException      IO 异常
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        // 如果已经有认证信息，跳过（避免重复处理）
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            log.debug("SecurityContext 已存在认证信息，跳过 JWT 验证");
            filterChain.doFilter(request, response);
            return;
        }

        // 从请求头提取 JWT Token
        String token = getTokenFromRequest(request);

        // 验证 Token 并设置认证信息
        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            // 从 Token 中解析用户信息
            String username = jwtTokenProvider.getUsernameFromToken(token);
            Long userId = jwtTokenProvider.getUserIdFromToken(token);

            // 创建 UserPrincipal 对象
            UserPrincipal principal = new UserPrincipal(userId, username);
            
            // 创建认证令牌（无需密码，因为已通过 JWT 验证）
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
            
            // 设置请求详情（IP、Session ID 等）
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 将认证信息存入 SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("用户认证成功: {}", username);
        } else if (StringUtils.hasText(token)) {
            // Token 存在但无效，记录警告日志
            log.warn("JWT Token 验证失败");
        }

        // 继续执行过滤器链
        filterChain.doFilter(request, response);
    }

    /**
     * 从 HTTP 请求头中提取 JWT Token
     * <p>
     * 期望的 Authorization 头格式：Bearer &lt;token&gt;
     *
     * @param request HTTP 请求对象
     * @return JWT Token 字符串，如果不存在或格式不正确则返回 null
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        // 获取 Authorization 请求头
        String bearerToken = request.getHeader("Authorization");
        
        // 检查 Token 是否存在且以 "Bearer " 开头
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // 去除 "Bearer " 前缀，返回纯 Token
            return bearerToken.substring(7);
        }
        return null;
    }
}
