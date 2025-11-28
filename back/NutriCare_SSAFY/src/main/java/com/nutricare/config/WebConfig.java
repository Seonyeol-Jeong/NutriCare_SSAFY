package com.nutricare.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.nutricare.interceptor.AdminInterceptor;
import com.nutricare.interceptor.JwtInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {
	
	@Autowired
	private JwtInterceptor jwtInterceptor;
	@Autowired
	private AdminInterceptor adminInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // "/images/**" 로 들어오는 모든 요청을
        // 로컬 컴퓨터의 "C:/nutricare_images/" 폴더로 매핑
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:///C:/nutricare_images/");
    }
    
    @Override
	public void addInterceptors(InterceptorRegistry registry) {
		// 1) 로그인 여부 확인(JWT로)
		registry.addInterceptor(jwtInterceptor)
				.addPathPatterns("/user/me", "/user/me/**", "/user/logout")
				.addPathPatterns("/admin/**")
				.addPathPatterns("/board-api")
				.addPathPatterns("/photo-api")
				.addPathPatterns("/file-api")
				.excludePathPatterns("/user/login", "/user", "/user/")
				.excludePathPatterns("/swagger-ui/**", "/v3/api-docs/**")
				.excludePathPatterns("/css/**", "/js/**", "/img/**", "/favicon.ico");
		
		// 2) 관리자 권한 확인
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/admin/**");
		
//		registry.addInterceptor(adminInterceptor).addPathPatterns("/users");
	}

}