package com.nutricare.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // "/images/**" 로 들어오는 모든 요청을
        // 로컬 컴퓨터의 "C:/nutricare_images/" 폴더로 매핑
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:///C:/nutricare_images/");
    }
}