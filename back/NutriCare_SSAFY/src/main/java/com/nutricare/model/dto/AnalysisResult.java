package com.nutricare.model.dto;

import java.time.LocalDateTime;

public class AnalysisResult {

    private Long analysisId;      // PK: analysis_result.analysis_id
    private Long photoId;         // FK: photo.photo_id
    private String diagnosisName; // 진단 결과 (예: '아토피', '정상' 등)
    private LocalDateTime createdAt; // 생성 일시

    // 기본 생성자
    public AnalysisResult() {
    }

    // 전체 필드 생성자 (조회용)
    public AnalysisResult(Long analysisId, Long photoId, String diagnosisName, LocalDateTime createdAt) {
        this.analysisId = analysisId;
        this.photoId = photoId;
        this.diagnosisName = diagnosisName;
        this.createdAt = createdAt;
    }

    // 데이터 삽입용 생성자 (ID, 날짜는 DB 자동 생성 가능하므로 제외 가능하지만, 편의상 포함)
    public AnalysisResult(Long photoId, String diagnosisName) {
        this.photoId = photoId;
        this.diagnosisName = diagnosisName;
    }

    // Getter & Setter
    public Long getAnalysisId() {
        return analysisId;
    }

    public void setAnalysisId(Long analysisId) {
        this.analysisId = analysisId;
    }

    public Long getPhotoId() {
        return photoId;
    }

    public void setPhotoId(Long photoId) {
        this.photoId = photoId;
    }

    public String getDiagnosisName() {
        return diagnosisName;
    }

    public void setDiagnosisName(String diagnosisName) {
        this.diagnosisName = diagnosisName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "AnalysisResult [analysisId=" + analysisId + ", photoId=" + photoId + 
               ", diagnosisName=" + diagnosisName + ", createdAt=" + createdAt + "]";
    }
}