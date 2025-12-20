package com.nutricare.config;

import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import com.nutricare.config.security.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity //스프링 Security 활성화
@EnableMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class SecurityConfig {
	
	private JwtAuthenticationFilter jwtAuthenticationFilter;
	
	public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
	}
	
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
		.cors(cors -> cors.configurationSource(corsConfigurationSource()))
		// 1. CSRF 비활성화 (JWT는 세션 기반이 아니라서 CSRF공격에 상대적으로 안전하므로 끈다)
		.csrf(csrf -> csrf.disable())
		// 2. 기본 로그인 폼 & HttpBasic 비활성화
		.formLogin(form -> form.disable())
		.httpBasic(basic -> basic.disable())
		// 3. 세션 관리 비활성화 (가장 중요! Security가 세션을 만들지도, 쓰지도 않게 설정)
		.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
		// 4. URL별 권한 설정 (기존 WebConfig의 인터셉터 설정을 여기서 대체합니다)
		.authorizeHttpRequests(auth -> auth
				.requestMatchers("/api/users/login", "/api/users").permitAll()
                .requestMatchers("/images/**", "/css/**", "/js/**", "/favicon.ico").permitAll()
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                .requestMatchers("/api/voice/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/boards/**").permitAll()
                // [관리자 전용] /admin 하위 모든 경로는 ADMIN 권한 필요 메서드
//                .requestMatchers("/api/admin/**").hasRole("ADMIN")
             // [나머지 모든 요청] 인증(로그인)된 사용자만 접근 가능
                .anyRequest().authenticated()
         )
		.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}
	
	private CorsConfigurationSource corsConfigurationSource() {
        return request -> {
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowedHeaders(Collections.singletonList("*"));
            config.setAllowedMethods(Collections.singletonList("*"));
            config.setAllowedOriginPatterns(Collections.singletonList("http://localhost:5173")); // 프론트엔드 주소 (Vue 기본 포트)
            config.setAllowCredentials(true);
            return config;
        };
    }
	
}
