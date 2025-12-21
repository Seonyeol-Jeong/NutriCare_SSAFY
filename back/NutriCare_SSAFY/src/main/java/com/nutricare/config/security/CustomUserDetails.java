package com.nutricare.config.security;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.nutricare.model.dto.User;

public class CustomUserDetails implements UserDetails, OAuth2User{
	
	private final User user;
	private Map<String, Object> attributes;
	
	
	public CustomUserDetails(User user) {
		this.user = user;
	}
	
	public CustomUserDetails(User user, Map<String, Object> attributes) {
		this.user = user;
		this.attributes = attributes;
	}

	public User getUser() {
		return user;
	}



	@Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
    }
	

	@Override
	public String getUsername() {
		return user.getEmail();
	}
	
	

	@Override
	public String getPassword() {
		// TODO Auto-generated method stub
		return user.getPasswordHash();
	}

	@Override
	public Map<String, Object> getAttributes() {
		// TODO Auto-generated method stub
		return attributes;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return user.getEmail();
	}
	
	
	// 계정 만료/잠김 여부 등은 현재 기능이 없으므로 true(정상)로 반환
	@Override
	public boolean isAccountNonExpired() { return true; }
	
	@Override
	public boolean isAccountNonLocked() { return true; }
	
	@Override
	public boolean isCredentialsNonExpired() { return true; }
	
	@Override
	public boolean isEnabled() {
		// isDeleted가 false여야 활성화된 계정
		return user.getIsDeleted() == null || !user.getIsDeleted();
	}
}
