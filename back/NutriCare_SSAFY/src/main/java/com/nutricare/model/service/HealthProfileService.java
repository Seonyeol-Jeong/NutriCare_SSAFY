package com.nutricare.model.service;

import com.nutricare.model.dto.HealthProfile;

public interface HealthProfileService {
    HealthProfile getHealthProfile(Long userId);
    boolean saveOrUpdateHealthProfile(HealthProfile healthProfile);

    /**
     * health_profile 기반 칼로리 플랜 계산(선택적으로 나이/성별 제공).
     * @param profile 필수: 키/몸무게/활동량/목표
     * @param ageYears 없으면 30세 기본값 사용
     * @param gender 성별(MALE/FEMALE), 없으면 남성 기준 오프셋
     */
    CalorieCalculator.CaloriePlan calculateCaloriePlan(HealthProfile profile, Integer ageYears, String gender);

    /**
     * userId로 health_profile 조회 후 칼로리 플랜 계산.
     */
    CalorieCalculator.CaloriePlan calculateCaloriePlan(Long userId, Integer ageYears, String gender);
}
