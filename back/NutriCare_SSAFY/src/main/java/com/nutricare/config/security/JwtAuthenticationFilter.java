package com.nutricare.config.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.nutricare.model.dto.User;
import com.nutricare.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private JwtUtil jwtUtil;

	public JwtAuthenticationFilter(JwtUtil jwtUtil) {
		this.jwtUtil = jwtUtil;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		// 1. 헤더에서 Authorization 추출
		String authHeader = request.getHeader("Authorization");
		String token = null;

		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			token = authHeader.substring(7);

			try {
				Long userId = jwtUtil.getUserIdFromToken(token);
				String email = jwtUtil.getEmailFromToken(token);
				String role = jwtUtil.getRoleFromToken(token);

				if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
					User userPrincipal = new User();
					userPrincipal.setUserId(userId);
					userPrincipal.setEmail(email);
					userPrincipal.setRole(role);

					UserDetails userDetails = new CustomUserDetails(userPrincipal);

					UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
							userDetails, null, userDetails.getAuthorities());

					authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(authentication);
				}
			} catch (Exception e) {
				logger.error("JWT Authentication Failed : " + e.getMessage());
			}
		}

		filterChain.doFilter(request, response);
	}

}
