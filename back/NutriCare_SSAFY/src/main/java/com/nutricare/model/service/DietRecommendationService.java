package com.nutricare.model.service;

import com.nutricare.model.dto.DietRecommendation;

public interface DietRecommendationService {

    /**
     * photoId 기반으로 최신 분석 결과를 찾아 diet_recommendation 생성.
     */
    DietRecommendation createByPhotoId(Long photoId, String memo);

    /**
     * analysisId 기반으로 diet_recommendation 생성.
     */
    DietRecommendation createByAnalysisId(Long analysisId, String memo);
}
