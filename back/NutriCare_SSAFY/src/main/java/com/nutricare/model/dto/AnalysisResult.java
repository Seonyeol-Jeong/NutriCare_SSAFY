package com.nutricare.model.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AnalysisResult {

    private Long analysisId;      // PK: analysis_result.analysis_id
    private Long photoId;         // FK: photo.photo_id
    @JsonProperty("diagnosis_name") // JSON의 diagnosis_name과 매핑
    private String diagnosisName; 
    
    @JsonProperty("prob_gunsun")
    private Double probGunsun;
    
    @JsonProperty("prob_atopy")
    private Double probAtopy;
    
    @JsonProperty("prob_acne")
    private Double probAcne;
    
    @JsonProperty("prob_normal")
    private Double probNormal;
    
    @JsonProperty("prob_rosacea")
    private Double probRosacea;
    
    @JsonProperty("prob_seborr")
    private Double probSeborr;
    
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
    @Override
    public String toString() {
        return "AnalysisResult [analysisId=" + analysisId + ", photoId=" + photoId + 
               ", diagnosisName=" + diagnosisName + ", createdAt=" + createdAt + "]";
    }

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

	public Double getProbGunsun() {
		return probGunsun;
	}

	public void setProbGunsun(Double probGunsun) {
		this.probGunsun = probGunsun;
	}

	public Double getProbAtopy() {
		return probAtopy;
	}

	public void setProbAtopy(Double probAtopy) {
		this.probAtopy = probAtopy;
	}

	public Double getProbAcne() {
		return probAcne;
	}

	public void setProbAcne(Double probAcne) {
		this.probAcne = probAcne;
	}

	public Double getProbNormal() {
		return probNormal;
	}

	public void setProbNormal(Double probNormal) {
		this.probNormal = probNormal;
	}

	public Double getProbRosacea() {
		return probRosacea;
	}

	public void setProbRosacea(Double probRosacea) {
		this.probRosacea = probRosacea;
	}

	public Double getProbSeborr() {
		return probSeborr;
	}

	public void setProbSeborr(Double probSeborr) {
		this.probSeborr = probSeborr;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
}