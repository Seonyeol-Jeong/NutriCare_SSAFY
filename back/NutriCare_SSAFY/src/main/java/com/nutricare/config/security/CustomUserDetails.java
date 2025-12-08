package com.nutricare.config.security;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.nutricare.model.dto.User;

public class CustomUserDetails implements UserDetails{
	
	private final User user;
	
	public CustomUserDetails(User user) {
		this.user = user;
	}
	
	public User getUser() {
		return user;
	}



	@Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // DB에 저장된 role (예: "ADMIN", "USER")을 Spring Security 권한으로 변환
        // 보통 "ROLE_" 접두어를 붙이는 것이 관례입니다.
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
    }
	

	@Override
	public String getUsername() {
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

	@Override
	public String getPassword() {
		// TODO Auto-generated method stub
		return null;
	}
}
