package com.nutricare.config.security;

import org.springframework.stereotype.Component;

import com.nutricare.model.dto.HealthProfile;
import com.nutricare.model.dto.User;
import com.nutricare.model.service.HealthProfileService;
import com.nutricare.model.service.UserService;

@Component
public class UserSecurity {
	
	private final HealthProfileService healthProfileService;
	
	public UserSecurity(HealthProfileService healthProfileService) {
		this.healthProfileService = healthProfileService;
	}
	
	
	public boolean isSelf(Long userId, CustomUserDetails userDetails) {
		if (userDetails == null) return false;
		
		if (userDetails.getUser().getRole().equals("ADMIN")) return true;
		
		return userDetails.getUser().getUserId().equals(userId);
	}
	
	public boolean isHealthProfileOwner(Long userId, CustomUserDetails userDetails) {
		if (userDetails == null) return false;
		
		if (userDetails.getUser().getRole().equals("ADMIN")) return true;
		
		HealthProfile healthProfile = healthProfileService.getHealthProfile(userId);
		
		if (healthProfile == null) return false;
		
		return userDetails.getUser().getUserId().equals(healthProfile.getUserId());
	}
	
}
