package com.nutricare.model.service;

import java.util.List;
import com.nutricare.model.dto.DietResult;

public interface DietResultService {
	// 수동 추가
	boolean insertDietResult(DietResult dietResult);
	
    // 1. 특정 추천(recId)에 해당하는 식단 목록 조회
    List<DietResult> getDietResultsByRecId(Long recId);
    
    // 2. 식단 상세 조회
    DietResult getDietResultById(Long mealId);
    
    // 3. 식단 수정
    boolean updateDietResult(DietResult dietResult);
    
    // 4. 식단 삭제
    boolean deleteDietResult(Long mealId);
    
    // 5. RAG 응답(JSON 문자열)을 파싱하여 식단 데이터 일괄 저장 (핵심)
    void saveDietResultsFromJson(Long recId, String jsonResponse);
    
    // 기존 식단 전체 삭제 (재생성 시 필요)
    boolean deleteDietResultsByRecId(Long recId);
}