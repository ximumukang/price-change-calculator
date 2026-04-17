package com.zang.pricechange.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 * 统一拦截 Controller 异常并返回标准格式的错误信息
 * 
 * 安全特性：
 * - 生产环境隐藏技术细节，仅返回通用错误消息
 * - 开发环境提供详细错误信息便于调试
 * - 所有异常均记录完整日志到服务端
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 当前激活的 Spring Profile
     * 用于区分开发和生产环境的错误响应
     */
    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    /**
     * 判断是否为生产环境
     */
    private boolean isProduction() {
        return "prod".equals(activeProfile) || "production".equals(activeProfile);
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.warn("参数校验失败: {}", errors);
        return Result.error(400, "参数校验失败");
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("非法参数: {}", ex.getMessage());
        // 生产环境返回通用消息，开发环境返回具体错误
        String message = isProduction() ? "请求参数错误" : ex.getMessage();
        return Result.error(400, message);
    }

    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleRuntimeException(RuntimeException ex) {
        log.error("运行时异常", ex);
        // 生产环境返回通用消息，避免泄露技术细节
        String message = isProduction() ? "请求处理失败" : ex.getMessage();
        return Result.error(400, message);
    }

    /**
     * 处理所有未捕获的通用异常
     * 这是最后一道防线，确保任何异常都不会暴露堆栈信息给客户端
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleGenericException(Exception ex) {
        // 完整日志仅记录到服务端，包含堆栈跟踪
        log.error("系统内部错误: {} - {}", ex.getClass().getName(), ex.getMessage(), ex);

        // 客户端仅返回通用错误消息，不暴露任何技术细节
        String message = isProduction() ? "服务器内部错误" : "系统异常: " + ex.getMessage();
        return Result.error(500, message);
    }
}
