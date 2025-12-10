package com.nutricare.model.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nutricare.model.dao.HealthProfileDao;
import com.nutricare.model.dto.HealthProfile;
import com.nutricare.model.service.CalorieCalculator;

@Service
public class HealthProfileServiceImpl implements HealthProfileService {

    private final HealthProfileDao healthProfileDao;

    public HealthProfileServiceImpl(HealthProfileDao healthProfileDao) {
        this.healthProfileDao = healthProfileDao;
    }

    @Override
    public HealthProfile getHealthProfile(Long userId) {
        return healthProfileDao.selectByUserId(userId);
    }

    @Override
    @Transactional
    public boolean saveOrUpdateHealthProfile(HealthProfile healthProfile) {
        // 1. 기존 프로필 존재 여부 확인
        HealthProfile existing = healthProfileDao.selectByUserId(healthProfile.getUserId());
        
        if (existing == null) {
            // 2. 신규 INSERT
            return healthProfileDao.insert(healthProfile) > 0;
        } else {
            // 3. UPDATE (ID는 기존 health_id 재사용)
            healthProfile.setHealthId(existing.getHealthId());
            return healthProfileDao.update(healthProfile) > 0;
        }
    }

    @Override
    public CalorieCalculator.CaloriePlan calculateCaloriePlan(HealthProfile profile, Integer ageYears, String gender) {
        return CalorieCalculator.calculate(profile, ageYears, gender);
    }

    @Override
    public CalorieCalculator.CaloriePlan calculateCaloriePlan(Long userId, Integer ageYears, String gender) {
        HealthProfile profile = getHealthProfile(userId);
        if (profile == null) {
            throw new IllegalArgumentException("health_profile not found for userId=" + userId);
        }
        return CalorieCalculator.calculate(profile, ageYears, gender);
    }
}
