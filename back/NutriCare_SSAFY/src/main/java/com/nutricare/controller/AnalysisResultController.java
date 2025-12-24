package com.nutricare.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nutricare.model.dto.AnalysisResult;
import com.nutricare.model.dto.Photo;
import com.nutricare.model.service.AiAnalysisApiService;
import com.nutricare.model.service.AnalysisResultService;
import com.nutricare.model.service.PhotoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/analysis-results")
@Tag(name = "AnalysisResult API", description = "AI 사진 진단 결과 관리 API")
public class AnalysisResultController {

    private final AnalysisResultService analysisResultService;

    public AnalysisResultController(AnalysisResultService analysisResultService) {
        this.analysisResultService = analysisResultService;
    }

    // 1. 진단 결과 저장
    // FastAPI나 클라이언트가 분석 완료 후 이 API를 호출하여 결과를 저장합니다.
    @Operation(summary = "진단 결과 저장", description = "AI가 분석한 진단 결과(diagnosis_name)를 DB에 저장합니다.")
    @PostMapping("")
    public ResponseEntity<?> saveResult(@RequestBody AnalysisResult analysisResult) {
        try {
            boolean isSaved = analysisResultService.save(analysisResult);
            if (isSaved) {
                // 저장된 객체(ID 포함) 반환
                return new ResponseEntity<>(analysisResult, HttpStatus.CREATED);
            }
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 2. 사진 ID로 진단 결과 조회
    // 사용자가 사진을 눌렀을 때 해당 사진의 진단 결과를 가져옵니다.
    @Operation(summary = "사진별 진단 결과 조회", description = "특정 사진(photoId)에 대한 최신 진단 결과를 조회합니다.")
    @GetMapping("/photos/{photoId}")
    public ResponseEntity<?> getByPhotoId(@PathVariable("photoId") Long photoId) {
        try {
            AnalysisResult result = analysisResultService.getByPhotoId(photoId);
            if (result != null) {
                return new ResponseEntity<>(result, HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // 3. 분석 ID로 상세 조회 (필요 시 사용)
    @Operation(summary = "진단 결과 상세 조회", description = "진단 ID(analysisId)로 상세 정보를 조회합니다.")
    @GetMapping("/{analysisId}")
    public ResponseEntity<?> getById(@PathVariable("analysisId") Long analysisId) {
        try {
            AnalysisResult result = analysisResultService.getById(analysisId);
            if (result != null) {
                return new ResponseEntity<>(result, HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}