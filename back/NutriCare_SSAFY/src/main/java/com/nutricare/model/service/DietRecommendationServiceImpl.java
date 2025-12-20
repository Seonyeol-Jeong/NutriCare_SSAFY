package com.nutricare.model.service;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nutricare.model.dao.AnalysisResultDao;
import com.nutricare.model.dao.DietRecommendationDao;
import com.nutricare.model.dao.HealthProfileDao;
import com.nutricare.model.dao.PhotoDao;
import com.nutricare.model.dto.AnalysisResult;
import com.nutricare.model.dto.DietRecommendation;
import com.nutricare.model.dto.HealthProfile;
import com.nutricare.model.dto.Photo;

@Service
public class DietRecommendationServiceImpl implements DietRecommendationService {

    private final DietRecommendationDao dietRecommendationDao;
    private final AnalysisResultDao analysisResultDao;
    private final PhotoDao photoDao;
    private final HealthProfileDao healthProfileDao;

    public DietRecommendationServiceImpl(DietRecommendationDao dietRecommendationDao,
                                         AnalysisResultDao analysisResultDao,
                                         PhotoDao photoDao,
                                         HealthProfileDao healthProfileDao) {
        this.dietRecommendationDao = dietRecommendationDao;
        this.analysisResultDao = analysisResultDao;
        this.photoDao = photoDao;
        this.healthProfileDao = healthProfileDao;
    }

    @Override
    @PreAuthorize("@dietSecurity.isPhotoOwner(#photoId, principal)")
    @Transactional
    public DietRecommendation createByPhotoId(Long photoId, String memo) {
        // photo -> analysis_result(최신) 조회
        AnalysisResult ar = analysisResultDao.selectByPhotoId(photoId);
        if (ar == null) {
            throw new IllegalArgumentException("analysis_result not found for photoId=" + photoId);
        }
        return createInternal(ar, memo);
    }

    @Override
    @PreAuthorize("@dietSecurity.isAnalysisOwner(#analysisId, principal)")
    @Transactional
    public DietRecommendation createByAnalysisId(Long analysisId, String memo) {
        // analysis_id 직접 지정
        AnalysisResult ar = analysisResultDao.selectById(analysisId);
        if (ar == null) {
            throw new IllegalArgumentException("analysis_result not found for analysisId=" + analysisId);
        }
        return createInternal(ar, memo);
    }

    private DietRecommendation createInternal(AnalysisResult ar, String memo) {
        // 분석 결과 -> photo -> user -> health_profile 순으로 FK 확인
        Photo photo = photoDao.selectOne(ar.getPhotoId());
        if (photo == null) {
            throw new IllegalArgumentException("photo not found. photoId=" + ar.getPhotoId());
        }
        HealthProfile hp = healthProfileDao.selectByUserId(photo.getUserId());
        if (hp == null) {
            throw new IllegalArgumentException("health_profile not found for userId=" + photo.getUserId());
        }
        DietRecommendation rec = new DietRecommendation(hp.getHealthId(), ar.getAnalysisId(), memo);
        int rows = dietRecommendationDao.insert(rec);
        if (rows <= 0 || rec.getRecId() == null) {
            throw new IllegalStateException("failed to insert diet_recommendation");
        }
        return rec;
    }
    
    @Override
    public DietRecommendation getByAnalysisId(Long analysisId) {
        return dietRecommendationDao.selectByAnalysisId(analysisId);
    }
}
