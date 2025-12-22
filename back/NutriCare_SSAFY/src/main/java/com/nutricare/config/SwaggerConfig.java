package com.nutricare.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI nutricareOpenAPI() {

        // JWT 입력을 Swagger 전체 API에 적용하도록 요구
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("JWT Token");

        // JWT Bearer Token 설정
        SecurityScheme securityScheme = new SecurityScheme()
                .name("Authorization")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        return new OpenAPI()
                .info(new Info()
                        .title("NutriCare API")
                        .description("NutriCare REST API 문서")
                        .version("v1.0.0")
                        .license(new License().name("SSAFY").url("https://www.ssafy.com")))
                .components(new Components()
                        .addSecuritySchemes("JWT Token", securityScheme))
                .addSecurityItem(securityRequirement);
    }
}
