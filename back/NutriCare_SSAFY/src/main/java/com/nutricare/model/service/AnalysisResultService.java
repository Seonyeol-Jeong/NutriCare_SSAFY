package com.nutricare.model.service;

import com.nutricare.model.dto.AnalysisResult;

public interface AnalysisResultService {

    // 분석 결과 저장
    boolean save(AnalysisResult analysisResult);

    // 사진 ID로 분석 결과 조회
    AnalysisResult getByPhotoId(Long photoId);

    // 분석 ID로 단건 조회
    AnalysisResult getById(Long analysisId);
}