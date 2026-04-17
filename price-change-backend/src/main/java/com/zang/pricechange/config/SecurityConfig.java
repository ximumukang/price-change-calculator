package com.zang.pricechange.config;

import com.zang.pricechange.security.ApiSignatureFilter;
import com.zang.pricechange.security.JwtAuthenticationFilter;
import com.zang.pricechange.security.RateLimitFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security 安全配置类
 * 负责配置应用的认证、授权、CORS等安全相关功能
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * JWT认证过滤器，用于验证请求中的JWT Token
     */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 速率限制过滤器，用于防止暴力破解和 DDoS 攻击
     */
    private final RateLimitFilter rateLimitFilter;

    /**
     * API 签名验证过滤器，用于防止请求篡改
     */
    private final ApiSignatureFilter apiSignatureFilter;

    /**
     * CORS允许的源地址列表
     * 从配置文件读取，默认为前端开发服务器地址
     */
    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:3000}")
    private List<String> allowedOrigins;

    /**
     * 配置Spring Security过滤链
     * 定义API的访问控制规则和安全策略
     *
     * @param http HttpSecurity对象，用于配置安全规则
     * @return 配置完成的SecurityFilterChain
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 禁用CSRF保护（因为使用JWT无状态认证）
                .csrf(csrf -> csrf.disable())
                // 启用CORS跨域支持，使用自定义配置
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 设置会话管理为无状态模式（不使用Session）
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 配置安全响应头
                .headers(headers -> headers
                        // X-Content-Type-Options: nosniff - 防止 MIME 类型嗅探
                        .contentTypeOptions(contentType -> contentType.disable())
                        // X-Frame-Options: DENY - 禁止嵌入 iframe，防止点击劫持
                        .frameOptions(frame -> frame.deny())
                        // X-XSS-Protection: 0 - 禁用浏览器 XSS 过滤（现代浏览器已内置）
                        .xssProtection(xss -> xss.disable())
                        // Content-Security-Policy: 内容安全策略，限制资源加载来源
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; " +
                                "script-src 'self' 'unsafe-inline'; " +
                                "style-src 'self' 'unsafe-inline'; " +
                                "img-src 'self' data: https:; " +
                                "font-src 'self'; " +
                                "connect-src 'self'; " +
                                "frame-ancestors 'none'; " +
                                "base-uri 'self'; " +
                                "form-action 'self'"
                        ))
                )
                // 配置请求授权规则
                .authorizeHttpRequests(auth -> auth
                        // 允许所有用户访问认证相关接口（登录、注册、获取公钥等）
                        .requestMatchers("/api/auth/**").permitAll()
                        // 允许访问 Swagger UI 和 API 文档
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll()
                        // 其他所有请求都需要认证
                        .anyRequest().authenticated()
                )
                // 在UsernamePasswordAuthenticationFilter之前添加JWT认证过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // 在JWT过滤器之前添加速率限制过滤器
                .addFilterBefore(rateLimitFilter, JwtAuthenticationFilter.class)
                // 在速率限制过滤器之前添加API签名验证过滤器
                .addFilterBefore(apiSignatureFilter, RateLimitFilter.class);

        return http.build();
    }

    /**
     * 配置CORS跨域资源共享策略
     * 允许前端应用跨域访问后端API
     *
     * @return CORS配置源
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 设置允许的源地址（从配置文件读取）
        configuration.setAllowedOrigins(allowedOrigins);
        // 设置允许的HTTP方法
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // 设置允许的请求头（明确列出，不使用 *）
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Requested-With"));
        // 设置暴露的响应头
        configuration.setExposedHeaders(List.of("Authorization"));
        // 允许携带凭证（如Cookie、Authorization头等）
        configuration.setAllowCredentials(true);
        // 预检请求缓存时间（秒）
        configuration.setMaxAge(3600L);

        // 将CORS配置应用到所有路径
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * 配置密码编码器
     * 使用BCrypt算法对密码进行加密存储
     * BCrypt是单向哈希算法，适合密码存储
     *
     * @return BCrypt密码编码器实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
