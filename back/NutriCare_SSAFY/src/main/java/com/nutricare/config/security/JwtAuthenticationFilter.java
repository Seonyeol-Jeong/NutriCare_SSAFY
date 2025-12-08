package com.nutricare.config.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.nutricare.model.dto.User;
import com.nutricare.model.service.UserService;
import com.nutricare.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter{

	
	private JwtUtil jwtUtil;
	private UserService userService;
	
	public JwtAuthenticationFilter(JwtUtil jwtUtil, UserService userService) {
		this.jwtUtil = jwtUtil;
		this.userService = userService;
	}
	
	

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		// 1. 헤더에서 Authorization 추출
		String authHeader = request.getHeader("Authorization");
		String token = null;
		Long userId = null;
		
		// 2. "Bearer"로 시작하는 지 확인
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			token = authHeader.substring(7); //"Bearer"제거
			try {
				//토큰에서 userId추출
				userId = jwtUtil.getUserIdFromToken(token);
			} catch (Exception e) {
				logger.warn(("JWT Token validation failed: ") + e.getMessage());
			}
		}
		
		//3. 토큰이 유효하고 (UserId있음), 현재 SecurityContext에 인증 정보가 없을 때
		if (userId != null &&  SecurityContextHolder.getContext().getAuthentication() == null) {
			//DB에서 유저 정보 조회
			User user = userService.getUserDetail(userId);
			
			if (user != null) {
				UserDetails userDetails = new CustomUserDetails(user);
				
				// Spring Security 인증 토큰 생성
				UsernamePasswordAuthenticationToken authentication =
						new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
				
				authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				
				// SecurityContext에 저장 (전역에서 로그인 정보 사용 가능)
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
		}
		
		filterChain.doFilter(request, response);
	}

}
