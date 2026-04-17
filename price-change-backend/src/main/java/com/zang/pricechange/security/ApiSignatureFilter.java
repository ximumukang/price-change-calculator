package com.zang.pricechange.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.TreeMap;

/**
 * API 签名验证过滤器
 * 对需要签名验证的请求进行 HMAC-SHA256 签名校验
 * 
 * 配置项：
 * - api.signature.enabled: 是否启用签名验证（默认 false）
 * - api.signature.secret: 签名密钥（从环境变量读取）
 * - api.signature.paths: 需要签名验证的路径前缀（默认 /api/secure/*）
 */
@Slf4j
@Component
public class ApiSignatureFilter extends OncePerRequestFilter {

    @Value("${api.signature.enabled:false}")
    private boolean signatureEnabled;

    @Value("${api.signature.secret:${API_SIGNATURE_SECRET:}}")
    private String signatureSecret;

    @Value("${api.signature.tolerance-ms:300000}")
    private long toleranceMs;

    /**
     * 判断当前请求路径是否需要签名验证
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!signatureEnabled) {
            return true; // 未启用签名验证，跳过过滤
        }
        
        String path = request.getRequestURI();
        // 仅对 /api/secure/* 路径进行签名验证
        return !path.startsWith("/api/secure/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // 获取签名相关请求头
        String signature = request.getHeader("X-API-Signature");
        String timestampStr = request.getHeader("X-API-Timestamp");
        
        if (signature == null || signature.isEmpty()) {
            log.warn("请求缺少签名: {}", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"请求缺少签名\"}");
            return;
        }
        
        if (timestampStr == null || timestampStr.isEmpty()) {
            log.warn("请求缺少时间戳: {}", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"请求缺少时间戳\"}");
            return;
        }
        
        // 检查密钥是否配置
        if (signatureSecret == null || signatureSecret.isEmpty()) {
            log.error("API 签名密钥未配置，请设置 API_SIGNATURE_SECRET 环境变量");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":500,\"message\":\"服务器配置错误\"}");
            return;
        }
        
        try {
            long timestamp = Long.parseLong(timestampStr);
            
            // 收集所有请求参数
            TreeMap<String, String> params = new TreeMap<>();
            request.getParameterMap().forEach((key, values) -> {
                if (values != null && values.length > 0) {
                    params.put(key, values[0]); // 取第一个值
                }
            });
            
            // 验证签名
            boolean isValid = ApiSignatureUtils.verifySignature(params, signatureSecret, signature, timestamp, toleranceMs);
            
            if (!isValid) {
                log.warn("签名验证失败: IP={}, URI={}", request.getRemoteAddr(), request.getRequestURI());
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":403,\"message\":\"签名验证失败\"}");
                return;
            }
            
            log.debug("签名验证成功: URI={}", request.getRequestURI());
            filterChain.doFilter(request, response);
            
        } catch (NumberFormatException e) {
            log.warn("无效的时间戳格式: {}", timestampStr);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":400,\"message\":\"无效的时间戳格式\"}");
        }
    }
}
