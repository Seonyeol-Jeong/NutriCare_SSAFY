package com.nutricare.model.dto;

import java.time.LocalDateTime;

/**
 * diet_recommendation 테이블 DTO.
 * - recId: 추천 식단 요청 ID
 * - healthId: health_profile FK
 * - analysisId: 얼굴 분석 FK (nullable)
 * - memo: 요청 시 메모
 */
public class DietRecommendation {

    private Long recId;         // PK
    private Long healthId;      // FK: health_profile.health_id
    private Long analysisId;    // FK: analysis_result.analysis_id (nullable)
    private String memo;        // 메모
    private LocalDateTime createdAt; // 생성 시각

    public DietRecommendation() {}

    public DietRecommendation(Long healthId, Long analysisId, String memo) {
        this.healthId = healthId;
        this.analysisId = analysisId;
        this.memo = memo;
    }

    public Long getRecId() { return recId; }
    public void setRecId(Long recId) { this.recId = recId; }

    public Long getHealthId() { return healthId; }
    public void setHealthId(Long healthId) { this.healthId = healthId; }

    public Long getAnalysisId() { return analysisId; }
    public void setAnalysisId(Long analysisId) { this.analysisId = analysisId; }

    public String getMemo() { return memo; }
    public void setMemo(String memo) { this.memo = memo; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
