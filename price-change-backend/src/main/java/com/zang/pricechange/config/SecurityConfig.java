package com.zang.pricechange.config;

import com.zang.pricechange.security.JwtAuthenticationFilter;
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
                // 配置请求授权规则
                .authorizeHttpRequests(auth -> auth
                        // 允许所有用户访问认证相关接口（登录、注册、获取公钥等）
                        .requestMatchers("/api/auth/**").permitAll()
                        // 其他所有请求都需要认证
                        .anyRequest().authenticated()
                )
                // 在UsernamePasswordAuthenticationFilter之前添加JWT认证过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

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
        // 设置允许的请求头（*表示允许所有）
        configuration.setAllowedHeaders(List.of("*"));
        // 允许携带凭证（如Cookie、Authorization头等）
        configuration.setAllowCredentials(true);

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
