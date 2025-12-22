package com.nutricare.model.dao;

import com.nutricare.model.dto.HealthProfile;

public interface HealthProfileDao {
    HealthProfile selectByUserId(Long userId);
    int insert(HealthProfile healthProfile);
    int update(HealthProfile healthProfile);
}