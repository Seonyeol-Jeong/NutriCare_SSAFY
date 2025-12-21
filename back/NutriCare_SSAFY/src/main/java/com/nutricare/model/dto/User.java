package com.nutricare.model.dto;

import java.time.LocalDateTime;

public class User {
	private Long userId;            // user_id
    private String email;           // email
    private String passwordHash;    // password_hash
    private String name;            // name
    private Integer birthYear;      // birth_year (YEAR → Integer)
    private String gender;          // gender ('MALE', 'FEMALE', 'OTHER')
    private LocalDateTime createdAt;// created_at
    private LocalDateTime updatedAt;// updated_at
    private Boolean isDeleted;      // is_deleted (TINYINT → Boolean)
    private String role;
    private String provider; // "google", "naver" 등
    private String providerId;
    
    // 기본 생성자
    public User() {}
    
    // 전체 생성자

	
	public String getRole() {
		return role;
	}

	public User(Long userId, String email, String passwordHash, String name, Integer birthYear, String gender,
			LocalDateTime createdAt, LocalDateTime updatedAt, Boolean isDeleted, String role, String provider,
			String providerId) {
		super();
		this.userId = userId;
		this.email = email;
		this.passwordHash = passwordHash;
		this.name = name;
		this.birthYear = birthYear;
		this.gender = gender;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.isDeleted = isDeleted;
		this.role = role;
		this.provider = provider;
		this.providerId = providerId;
	}

	public void setRole(String role) {
		this.role = role;
	}

	// 회원가입 시 받을 생성자
	public User(String email, String passwordHash, String name, Integer birthYear, String gender) {
		super();
		this.email = email;
		this.passwordHash = passwordHash;
		this.name = name;
		this.birthYear = birthYear;
		this.gender = gender;
	}

	// getter & setter
	public Long getUserId() {
		return userId;
	}


	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getBirthYear() {
		return birthYear;
	}

	public void setBirthYear(Integer birthYear) {
		this.birthYear = birthYear;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public Boolean getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(Boolean isDeleted) {
		this.isDeleted = isDeleted;
	}
	
	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getProviderId() {
		return providerId;
	}

	public void setProviderId(String providerId) {
		this.providerId = providerId;
	}

	@Override
	public String toString() {
		return "User [userId=" + userId + ", email=" + email + ", passwordHash=" + passwordHash + ", name=" + name
				+ ", birthYear=" + birthYear + ", gender=" + gender + ", createdAt=" + createdAt + ", updatedAt="
				+ updatedAt + ", isDeleted=" + isDeleted + ", role=" + role + ", provider=" + provider + ", providerId="
				+ providerId + "]";
	}

}
