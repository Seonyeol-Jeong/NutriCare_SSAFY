package com.nutricare.config.security;

import com.nutricare.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil; // 기존에 만들어둔 토큰 생성기

    public OAuth2SuccessHandler(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // 1. 인증된 유저 정보 꺼내기 (UserService에서 리턴한 그 객체)
    	CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
    	
        Long userId = userDetails.getUser().getUserId();
        String email = userDetails.getUser().getEmail();
        String role = userDetails.getUser().getRole();
        

        // 2. JWT 토큰 생성 (기존 JwtUtil 재활용!)
        // ★ 중요: JwtUtil.createToken() 메서드가 String(email)을 받는지, 
        // Long(userId)을 받는지 확인하고 맞춰주세요. 보통 email로 많이 만듭니다.
        String accessToken = jwtUtil.generateToken(userId, email, role);

        System.out.println("토큰 발급 성공: " + accessToken);

        // 3. 프론트엔드로 리다이렉트 (토큰을 URL 뒤에 붙여서 보냄)
        // 예: http://localhost:5173/oauth/callback?token=eyJhb...
        String targetUrl = "http://localhost:5173/oauth/callback?token=" + accessToken;
        
        response.sendRedirect(targetUrl);
    }
}