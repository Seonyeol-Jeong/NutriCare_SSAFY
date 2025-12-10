package com.nutricare.model.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nutricare.model.dao.DietContextDao;
import com.nutricare.model.dto.DietContext;

@Service
public class DietContextServiceImpl implements DietContextService {

	private final DietContextDao dietContextDao;

	public DietContextServiceImpl(DietContextDao dietContextDao) {
		this.dietContextDao = dietContextDao;
	}

	@Override
	@Transactional(readOnly = true)
	public DietContext getContextForRec(Long recId) {
		DietContext ctx = dietContextDao.findDietContextByRecId(recId);
		if (ctx == null) {
			throw new IllegalArgumentException("diet_recommendation not found. recId=" + recId);
		}
		return ctx;
	}

}
