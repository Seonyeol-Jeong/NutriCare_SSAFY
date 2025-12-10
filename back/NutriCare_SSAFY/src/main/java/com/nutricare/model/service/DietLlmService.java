package com.nutricare.model.service;

import com.nutricare.model.dto.DietContext;
import com.nutricare.model.service.CalorieCalculator.CaloriePlan;

public interface DietLlmService {
	/**
	 * 컨텍스트 + 칼로리 플랜을 포함한 요청을 LLM에 전달한다.
	 */
	String requestDietGeneration(DietContext context, CaloriePlan plan);
}
