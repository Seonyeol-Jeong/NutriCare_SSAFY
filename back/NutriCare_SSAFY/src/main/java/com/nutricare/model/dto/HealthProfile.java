package com.nutricare.model.dto;

import java.time.LocalDateTime;

public class HealthProfile {

    private Long healthId;          // health_id (PK)
    private Long userId;            // user_id (FK)
    private Double heightCm;        // height_cm
    private Double weightKg;        // weight_kg
    private String activityLevel;   // activity_level (ENUM: 'LOW','MEDIUM','HIGH')
    private String goalType;        // goal_type (ENUM: 'LOSE','MAINTAIN','GAIN')
    private LocalDateTime updatedAt;// updated_at

    public HealthProfile() {}

    public HealthProfile(Long userId, Double heightCm, Double weightKg, String activityLevel, String goalType) {
        this.userId = userId;
        this.heightCm = heightCm;
        this.weightKg = weightKg;
        this.activityLevel = activityLevel;
        this.goalType = goalType;
    }

    // Getters & Setters
    public Long getHealthId() { return healthId; }
    public void setHealthId(Long healthId) { this.healthId = healthId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Double getHeightCm() { return heightCm; }
    public void setHeightCm(Double heightCm) { this.heightCm = heightCm; }

    public Double getWeightKg() { return weightKg; }
    public void setWeightKg(Double weightKg) { this.weightKg = weightKg; }

    public String getActivityLevel() { return activityLevel; }
    public void setActivityLevel(String activityLevel) { this.activityLevel = activityLevel; }

    public String getGoalType() { return goalType; }
    public void setGoalType(String goalType) { this.goalType = goalType; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "HealthProfile [healthId=" + healthId + ", userId=" + userId + 
               ", heightCm=" + heightCm + ", weightKg=" + weightKg + 
               ", activityLevel=" + activityLevel + ", goalType=" + goalType + 
               ", updatedAt=" + updatedAt + "]";
    }
}