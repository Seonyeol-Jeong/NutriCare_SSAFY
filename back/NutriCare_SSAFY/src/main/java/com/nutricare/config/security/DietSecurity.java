package com.nutricare.config.security;

import org.springframework.stereotype.Component;
import com.nutricare.controller.DietRecommendationController.CreateRequest;
import com.nutricare.model.dao.DietResultDao;
import com.nutricare.model.dto.AnalysisResult;
import com.nutricare.model.dto.DietContext;
import com.nutricare.model.dto.DietRecommendation;
import com.nutricare.model.dto.DietResult;
import com.nutricare.model.dto.Photo;
import com.nutricare.model.service.AnalysisResultService;
import com.nutricare.model.service.DietContextService;
import com.nutricare.model.service.DietRecommendationService;
import com.nutricare.model.service.DietResultService;
import com.nutricare.model.service.PhotoService;

@Component
public class DietSecurity {

    private final DietContextService dietContextService;
    private final PhotoService photoService;
    private final AnalysisResultService analysisResultService;
    private final DietResultDao dietResultDao;

    public DietSecurity(DietContextService dietContextService, 
    		PhotoService photoService, 
    		AnalysisResultService analysisResultService,
    		DietResultDao dietResultDao,
    		DietRecommendationService dietRecommendationService) {
        this.dietContextService = dietContextService;
        this.photoService = photoService;
        this.analysisResultService = analysisResultService;
        this.dietResultDao = dietResultDao;
    }

    // 1. [생성 시] 요청 데이터(CreateRequest)가 내 것인지 확인
    // (기존 checkAuthorityByUserId + resolveOwnerUserId 로직 통합)
    public boolean isSourceOwner(CreateRequest request, CustomUserDetails user) {
        if (user == null) return false;
        if ("ADMIN".equals(user.getUser().getRole())) return true;

        Long ownerId = null;
        
        // PhotoId가 있으면 사진 주인 확인
        if (request.getPhotoId() != null) {
            Photo photo = photoService.selectOne(request.getPhotoId());
            if (photo != null) ownerId = photo.getUserId();
        } 
        // AnalysisId가 있으면 분석 결과 주인 확인
        else if (request.getAnalysisId() != null) {
            AnalysisResult ar = analysisResultService.getById(request.getAnalysisId());
            if (ar != null) {
                 Photo photo = photoService.selectOne(ar.getPhotoId());
                 if (photo != null) ownerId = photo.getUserId();
            }
        }

        return ownerId != null && ownerId.equals(user.getUser().getUserId());
    }

    // 2. [조회/삭제 시] 식단 추천(recId)이 내 것인지 확인
    // (기존 checkAuthorityByRecId 로직)
    public boolean isRecOwner(Long recId, CustomUserDetails userDetails) {
        if (userDetails == null) return false;
        if ("ADMIN".equals(userDetails.getUser().getRole())) return true;

        DietContext context = dietContextService.getContextForRec(recId);
        return context != null && context.getUserId().equals(userDetails.getUser().getUserId());
    }
    
    public boolean isPhotoOwner (Long photoId, CustomUserDetails userDetails) {
        if (userDetails == null) return false;
        if ("ADMIN".equals(userDetails.getUser().getRole())) return true;
        
        Photo photo = photoService.selectOne(photoId);
        
        if (photo == null) return false;
        
        return userDetails.getUser().getUserId().equals(photo.getUserId());
    }
    
    public boolean isAnalysisOwner (Long analysisId, CustomUserDetails userDetails) {
        if (userDetails == null) return false;
        if ("ADMIN".equals(userDetails.getUser().getRole())) return true;
        
        AnalysisResult analysisResult = analysisResultService.getById(analysisId);
        if (analysisResult == null) return false;
        Photo photo = photoService.selectOne(analysisResult.getPhotoId());
        if (photo == null) return false;
        
        return userDetails.getUser().getUserId().equals(photo.getUserId());
    }
    
    public boolean isDietResultOwner (Long resultId, CustomUserDetails userDetails) { 
        if (userDetails == null) return false;
        if ("ADMIN".equals(userDetails.getUser().getRole())) return true;
        
        DietResult dietResult = dietResultDao.selectByResultId(resultId);
        
        if (dietResult == null) return false;
        
        return isRecOwner(dietResult.getRecId(), userDetails);
    }
}