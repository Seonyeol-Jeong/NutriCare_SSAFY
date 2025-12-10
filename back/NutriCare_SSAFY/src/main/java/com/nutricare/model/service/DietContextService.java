package com.nutricare.model.service;

import com.nutricare.model.dto.DietContext;

public interface DietContextService {

    DietContext getContextForRec(Long recId);
}
