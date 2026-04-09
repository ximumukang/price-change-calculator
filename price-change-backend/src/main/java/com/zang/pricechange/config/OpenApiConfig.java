package com.zang.pricechange.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("涨跌幅计算 API")
                        .description("涨跌幅计算服务 RESTful API 文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("zang")
                                .email("zang@example.com")));
    }
}