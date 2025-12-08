package com.nutricare.model.dto;

public class UserDetailResponse {

    private User user;                // 기본 회원 정보
    private HealthProfile healthProfile; // 건강 정보 (없을 수도 있음)

    public UserDetailResponse(User user, HealthProfile healthProfile) {
        this.user = user;
        this.healthProfile = healthProfile;
    }

    public User getUser() {
        return user;
    }

    public HealthProfile getHealthProfile() {
        return healthProfile;
    }
}