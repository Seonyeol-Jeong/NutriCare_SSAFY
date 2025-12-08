package com.nutricare.model.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nutricare.model.dao.AnalysisResultDao;
import com.nutricare.model.dto.AnalysisResult;

@Service
public class AnalysisResultServiceImpl implements AnalysisResultService {

    private final AnalysisResultDao analysisResultDao;

    public AnalysisResultServiceImpl(AnalysisResultDao analysisResultDao) {
        this.analysisResultDao = analysisResultDao;
    }

    @Override
    @Transactional
    public boolean save(AnalysisResult analysisResult) {
        // insert 성공 시 1 반환 -> true
        return analysisResultDao.insert(analysisResult) > 0;
    }

    @Override
    public AnalysisResult getByPhotoId(Long photoId) {
        return analysisResultDao.selectByPhotoId(photoId);
    }

    @Override
    public AnalysisResult getById(Long analysisId) {
        return analysisResultDao.selectById(analysisId);
    }
}