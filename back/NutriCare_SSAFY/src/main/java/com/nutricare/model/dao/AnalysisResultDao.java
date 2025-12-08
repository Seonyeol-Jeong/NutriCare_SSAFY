package com.nutricare.model.dao;

import com.nutricare.model.dto.AnalysisResult;

public interface AnalysisResultDao {

    // 1. 분석 결과 저장
    int insert(AnalysisResult analysisResult);

    // 2. 사진 ID로 분석 결과 조회 (사진 1장당 결과 1개라고 가정)
    AnalysisResult selectByPhotoId(Long photoId);

    // 3. 분석 ID로 단건 조회
    AnalysisResult selectById(Long analysisId);
    
    // 4. 삭제 (필요 시 사용)
    int delete(Long analysisId);
}