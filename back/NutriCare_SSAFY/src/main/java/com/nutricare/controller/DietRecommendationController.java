package com.nutricare.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.nutricare.config.security.CustomUserDetails;
import com.nutricare.model.dto.DietContext;
import com.nutricare.model.dto.DietRecommendation;
import com.nutricare.model.dto.DietResult;
import com.nutricare.model.dto.HealthProfile;
import com.nutricare.model.service.AnalysisResultService;
import com.nutricare.model.service.CalorieCalculator;
import com.nutricare.model.service.DietContextService;
import com.nutricare.model.service.DietLlmService;
import com.nutricare.model.service.DietRuleEngine;
import com.nutricare.model.service.DietRecommendationService;
import com.nutricare.model.service.DietResultService;
import com.nutricare.model.service.HealthProfileService;
import com.nutricare.model.service.PhotoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/diet-recommendations")
@Tag(name = "DietRecommendation API", description = "식단 추천 생성/조회 API")
public class DietRecommendationController {

    public static class CreateRequest {
        private Long photoId;
        private Long analysisId;
        private String memo;
        public Long getPhotoId() { return photoId; }
        public void setPhotoId(Long photoId) { this.photoId = photoId; }
        public Long getAnalysisId() { return analysisId; }
        public void setAnalysisId(Long analysisId) { this.analysisId = analysisId; }
        public String getMemo() { return memo; }
        public void setMemo(String memo) { this.memo = memo; }
    }

    private final DietContextService dietContextService;
    private final DietLlmService dietLlmService;
    private final DietRuleEngine dietRuleEngine;
    private final DietResultService dietResultService;
    private final DietRecommendationService dietRecommendationService;
    private final HealthProfileService healthProfileService;

    public DietRecommendationController(DietContextService dietContextService,
                                        DietLlmService dietLlmService,
                                        DietRuleEngine dietRuleEngine,
                                        DietResultService dietResultService,
                                        DietRecommendationService dietRecommendationService,
                                        PhotoService photoService,
                                        AnalysisResultService analysisResultService,
                                        HealthProfileService healthProfileService) {
        this.dietContextService = dietContextService;
        this.dietLlmService = dietLlmService;
        this.dietRuleEngine = dietRuleEngine;
        this.dietResultService = dietResultService;
        this.dietRecommendationService = dietRecommendationService;
        this.healthProfileService = healthProfileService;
    }

    // ===========================
    // 0. 식단 추천 rec 생성 (photoId/analysisId 기반)
    // ===========================
    @Operation(summary = "식단 추천 rec 생성", description = "photoId 또는 analysisId를 기반으로 diet_recommendation을 생성합니다.")
    @PostMapping("/create")
    public ResponseEntity<?> createRec(@RequestBody CreateRequest body) {
    	DietRecommendation rec;
    	
        if (body.getPhotoId() != null) {
            rec = dietRecommendationService.createByPhotoId(body.getPhotoId(), body.getMemo());
        } else {
            rec = dietRecommendationService.createByAnalysisId(body.getAnalysisId(), body.getMemo());
        }
        
        if (rec == null) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        
        return new ResponseEntity<DietRecommendation>(rec, HttpStatus.CREATED);
    }

    // ===========================
    // 1. AI 식단 추천 생성 (Action)
    //    - 컨텍스트 + (옵션) 칼로리 플랜을 FastAPI로 전달
    //    - 프롬프트/룰 구성은 Python 단 처리
    // ===========================
    @Operation(summary = "AI 식단 추천 생성", description = "추천 기록(recId) 컨텍스트를 AI에 전달하고 응답을 diet_result에 저장합니다.")
    @PreAuthorize("@dietSecurity.isRecOwner(#recId, principal)")
    @PostMapping("/{recId}")
    public ResponseEntity<?> generateDietResult(
            @Parameter(description = "추천 식단 기록 ID", required = true)
            @PathVariable Long recId) {

        try {
            // 1) 컨텍스트 조회
            DietContext context = dietContextService.getContextForRec(recId);

            // 2) 건강 프로필이 있으면 칼로리 플랜 계산 (없으면 null 전달)
            HealthProfile hp = healthProfileService.getHealthProfile(context.getUserId());
            CalorieCalculator.CaloriePlan plan = null;
            if (hp != null) {
                Integer ageYears = (context.getBirthYear() != null)
                        ? (java.time.LocalDate.now().getYear() - context.getBirthYear())
                        : null;
                plan = healthProfileService.calculateCaloriePlan(hp, ageYears, context.getGender());
            }

            // 3) FastAPI/LLM 호출 (프롬프트 구성은 Python 단 처리)
            DietRuleEngine.RuleText ruleText = dietRuleEngine.buildRules(context, plan);
            String responseJson = dietLlmService.requestDietGeneration(context, plan, ruleText.toString());

            // 4) 응답 JSON을 diet_result에 저장
            dietResultService.saveDietResultsFromJson(recId, responseJson);
            return ResponseEntity.ok(responseJson);
        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().body(iae.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ===========================
    // 2. 추천 식단 목록 조회 (List)
    // ===========================
    @Operation(summary = "추천 식단 목록 조회", description = "추천 기록(recId)에 매핑된 모든 메뉴 리스트를 조회합니다.")
    @PreAuthorize("@dietSecurity.isRecOwner(#recId, principal)")
    @GetMapping("/{recId}")
    public ResponseEntity<?> getListByRecId(
            @Parameter(description = "추천 식단 기록 ID", required = true)
            @PathVariable("recId") Long recId) {
    	
            List<DietResult> list = dietResultService.getDietResultsByRecId(recId);
            if (list != null && !list.isEmpty()) {
                return new ResponseEntity<>(list, HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // ===========================
    // 3. 추천 식단 상세 조회 (Detail)
    // ===========================
    @Operation(summary = "추천 식단 상세 조회", description = "개별 메뉴(resultId) 상세 정보를 조회합니다.")
    
    @GetMapping("/results/{resultId}") // URL: /api/diet-recommendations/results/{resultId}
    public ResponseEntity<?> getDetail(
            @Parameter(description = "개별 식단 결과 ID (diet_result PK)", required = true)
            @PathVariable("resultId") Long resultId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            DietResult result = dietResultService.getDietResultById(resultId);
            if (result != null) {
                checkAuthorityByRecId(result.getRecId(), userDetails); // 소유자 확인
                return new ResponseEntity<>(result, HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ===========================
    // 4. 추천 식단 삭제 (Delete)
    // ===========================
    @Operation(summary = "추천 식단 삭제", description = "지정된 메뉴(resultId)를 삭제합니다.")
    @DeleteMapping("/results/{resultId}") // URL: /api/diet-recommendations/results/{resultId}
    public ResponseEntity<?> delete(
            @PathVariable("resultId") Long resultId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            DietResult origin = dietResultService.getDietResultById(resultId);
            
            if (origin == null) {
                return new ResponseEntity<>("식단 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
            }
            
            checkAuthorityByRecId(origin.getRecId(), userDetails); // 소유자 확인
            
            boolean isDeleted = dietResultService.deleteDietResult(resultId);
            if (isDeleted) {
                return new ResponseEntity<>(HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @Operation(summary = "분석 ID로 식단 추천 조회", description = "특정 분석 결과(analysisId)에 연결된 식단 추천 정보를 조회합니다.")
    @GetMapping("/analysis/{analysisId}")
    public ResponseEntity<?> getRecByAnalysisId(@PathVariable Long analysisId) {
    	// 1. 서비스에서 analysisId로 조회 (서비스에 이 메서드가 없다면 만들어야 함!)
    	DietRecommendation rec = dietRecommendationService.getByAnalysisId(analysisId);
    	
    	if (rec != null) {
    		return new ResponseEntity<>(rec, HttpStatus.OK);
    	} else {
    		// 없으면 404 Not Found를 보내서 프론트의 catch 블록(404 체크)이 동작하게 함
    		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    	}
    }

    
    // ===========================
    // Helper Method (권한 검사)
    // ===========================


    private void checkAuthorityByRecId(Long recId, CustomUserDetails userDetails) {
        DietContext context = dietContextService.getContextForRec(recId);
        if (userDetails.getUser().getRole().equals("ADMIN")) {
            return;
        }
        if (!context.getUserId().equals(userDetails.getUser().getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 식단에 대한 접근 권한이 없습니다.");
        }
    }
}
