package com.nutricare.model.service;

import com.nutricare.model.dto.AnalysisResult;

public interface AiAnalysisApiService {
	// FastAPI에 분석 요청을 보내고 진단명(diagnosis_name)을 반환받음
    AnalysisResult requestAnalysis(Long photoId, String photoUrl);
}
