package com.doan.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI().info(
                new Info()
                        .title("Food Map Backend API")
                        .description("Base backend theo cấu trúc đồ án quán ăn / món ăn")
                        .version("1.0.0"));
    }
}
