package com.zang.pricechange.aspect;

import com.zang.pricechange.annotation.AuditLog;
import com.zang.pricechange.security.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 操作日志审计切面
 * 记录被 @AuditLog 注解标记的方法的执行情况
 */
@Slf4j
@Aspect
@Component
public class AuditLogAspect {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Around("@annotation(com.zang.pricechange.annotation.AuditLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        AuditLog auditLog = method.getAnnotation(AuditLog.class);

        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

        // 获取当前用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = "anonymous";
        Long userId = null;
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            username = principal.getUsername();
            userId = principal.getUserId();
        }

        // 记录开始时间
        long startTime = System.currentTimeMillis();
        String operationTime = LocalDateTime.now().format(FORMATTER);

        log.info("=== 操作日志 ===");
        log.info("时间: {}", operationTime);
        log.info("用户: {} (ID: {})", username, userId);
        log.info("操作: {} - {}", auditLog.operationType().getDescription(), auditLog.value());
        log.info("方法: {}.{}", signature.getDeclaringTypeName(), method.getName());
        if (request != null) {
            log.info("IP: {}", getClientIp(request));
            log.info("URI: {}", request.getRequestURI());
            log.info("Method: {}", request.getMethod());
        }

        Object result = null;
        Throwable exception = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            exception = e;
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            if (exception != null) {
                log.warn("操作失败: {}, 耗时: {}ms", exception.getMessage(), duration);
            } else {
                log.info("操作成功, 耗时: {}ms", duration);
            }
            log.info("===============");
        }
    }

    /**
     * 获取客户端真实 IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
