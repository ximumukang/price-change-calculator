package com.zang.pricechange.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zang.pricechange.common.Result;
import com.zang.pricechange.config.RateLimitConfig;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 速率限制过滤器
 * 对登录接口和通用 API 进行限流保护
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitConfig rateLimitConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        String clientIp = getClientIp(request);

        // 只对登录接口进行严格的限流
        if (uri.startsWith("/api/auth/login") || uri.startsWith("/api/auth/register")) {
            Bucket bucket = rateLimitConfig.resolveLoginBucket(clientIp);
            if (!bucket.tryConsume(1)) {
                log.warn("登录接口限流触发 - IP: {}", clientIp);
                sendRateLimitResponse(response);
                return;
            }
        } else if (uri.startsWith("/api/")) {
            // 对其他 API 接口进行宽松限流
            Bucket bucket = rateLimitConfig.resolveApiBucket(clientIp);
            if (!bucket.tryConsume(1)) {
                log.warn("API 限流触发 - IP: {}, URI: {}", clientIp, uri);
                sendRateLimitResponse(response);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 获取客户端真实 IP 地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果是多个 IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 发送限流响应
     */
    private void sendRateLimitResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        Result<Void> result = Result.error(429, "请求过于频繁，请稍后再试");
        objectMapper.writeValue(response.getWriter(), result);
    }
}
