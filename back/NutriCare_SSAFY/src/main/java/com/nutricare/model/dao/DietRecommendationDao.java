package com.nutricare.model.dao;

import com.nutricare.model.dto.DietRecommendation;

public interface DietRecommendationDao {

    // diet_recommendation 신규 생성
    int insert(DietRecommendation rec);

    // rec_id로 단건 조회.
    DietRecommendation selectById(Long recId);
    
    DietRecommendation selectByAnalysisId(Long analysisId);
}
