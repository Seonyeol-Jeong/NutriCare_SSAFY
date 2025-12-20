package com.nutricare.model.service;

public interface AiAnalysisApiService {
	// FastAPI에 분석 요청을 보내고 진단명(diagnosis_name)을 반환받음
    String requestAnalysis(Long photoId, String photoUrl);
}
