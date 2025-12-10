package com.nutricare.model.dto;

import java.time.LocalDateTime;

/**
 * 식단 생성 시 컨텍스트 정보 DTO (user + health_profile + analysis_result + diet_recommendation).
 */
public class DietContext {

    // ===== user =====
	private Long userId;
    private String userName;
    private Integer birthYear;
    private String gender;

    // ===== health_profile =====
    private Double heightCm;
    private Double weightKg;
    private String activityLevel;
    private String healthGoalType;
    private LocalDateTime healthUpdatedAt;

    // ===== analysis_result =====
    private String diagnosisName;
    private LocalDateTime analysisCreatedAt;

    // ===== diet_recommendation =====
    private String recMemo;
    private LocalDateTime recCreatedAt;

    // ===== Getter/Setter =====

    public Long getUserId() {return userId;}
    public void setUserId(Long userId) {this.userId = userId;}
    
    public String getUserName() { return userName; }
	public void setUserName(String userName) { this.userName = userName; }

    public Integer getBirthYear() { return birthYear; }
    public void setBirthYear(Integer birthYear) { this.birthYear = birthYear; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public Double getHeightCm() { return heightCm; }
    public void setHeightCm(Double heightCm) { this.heightCm = heightCm; }

    public Double getWeightKg() { return weightKg; }
    public void setWeightKg(Double weightKg) { this.weightKg = weightKg; }

    public String getActivityLevel() { return activityLevel; }
    public void setActivityLevel(String activityLevel) { this.activityLevel = activityLevel; }

    public String getHealthGoalType() { return healthGoalType; }
    public void setHealthGoalType(String healthGoalType) { this.healthGoalType = healthGoalType; }

    public LocalDateTime getHealthUpdatedAt() { return healthUpdatedAt; }
    public void setHealthUpdatedAt(LocalDateTime healthUpdatedAt) { this.healthUpdatedAt = healthUpdatedAt; }

    public String getDiagnosisName() { return diagnosisName; }
    public void setDiagnosisName(String diagnosisName) { this.diagnosisName = diagnosisName; }

    public LocalDateTime getAnalysisCreatedAt() { return analysisCreatedAt; }
    public void setAnalysisCreatedAt(LocalDateTime analysisCreatedAt) { this.analysisCreatedAt = analysisCreatedAt; }

    public String getRecMemo() { return recMemo; }
    public void setRecMemo(String recMemo) { this.recMemo = recMemo; }

    public LocalDateTime getRecCreatedAt() { return recCreatedAt; }
    public void setRecCreatedAt(LocalDateTime recCreatedAt) { this.recCreatedAt = recCreatedAt; }
}
