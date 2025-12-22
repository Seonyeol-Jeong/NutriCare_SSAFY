package com.nutricare.model.service;

import com.nutricare.model.dto.DietContext;
import com.nutricare.model.service.CalorieCalculator.CaloriePlan;

public interface DietLlmService {
    /**
     * 컨텍스트 + 칼로리플랜 + ruleText(허용/제한 조건)를 합쳐서 LLM에 전달한다.
     */
    String requestDietGeneration(DietContext context, CaloriePlan plan, String ruleText);
}
