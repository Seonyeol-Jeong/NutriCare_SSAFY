package com.nutricare.model.dao;

import com.nutricare.model.dto.DietContext;

public interface DietContextDao {

    DietContext findDietContextByRecId(Long recId);
}
