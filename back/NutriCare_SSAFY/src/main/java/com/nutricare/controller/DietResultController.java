package com.nutricare.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nutricare.model.dto.DietResult;
import com.nutricare.model.service.DietResultService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/diet-api")
@Tag(name = "DietResult API", description = "생성된 식단 상세 결과(메뉴) 관리 API")
public class DietResultController {

    private final DietResultService dietResultService;

    @Autowired
    public DietResultController(DietResultService dietResultService) {
        this.dietResultService = dietResultService;
    }

    // 1. 특정 추천(rec_id)에 대한 식단 목록 조회
    // RAG가 생성해준 식단 리스트를 프론트엔드에서 보여줄 때 사용합니다.
    @Operation(summary = "추천 식단 목록 조회", description = "특정 추천 기록(recId)에 포함된 모든 메뉴 리스트를 조회합니다.")
    @GetMapping("/result/list/{recId}")
    public ResponseEntity<?> getListByRecId(@PathVariable("recId") Long recId) {
        try {
            List<DietResult> list = dietResultService.getDietResultsByRecId(recId);
            if (list != null && !list.isEmpty()) {
                return new ResponseEntity<>(list, HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 2. 식단 상세 조회 (단건)
    // 메뉴 하나를 클릭해서 상세 정보를 볼 때 사용합니다.
    @Operation(summary = "식단 상세 조회", description = "특정 식단(mealId)의 상세 정보를 조회합니다.")
    @GetMapping("/result/{mealId}")
    public ResponseEntity<?> getDetail(@PathVariable("mealId") Long mealId) {
        try {
            DietResult result = dietResultService.getDietResultById(mealId);
            if (result != null) {
                return new ResponseEntity<>(result, HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 3. 식단 정보 수정
    // 사용자가 RAG가 짜준 식단이 마음에 안 들어서 칼로리나 메뉴명을 직접 고칠 때 사용합니다.
    @Operation(summary = "식단 정보 수정", description = "식단 상세 정보를 수정합니다.")
    @PutMapping("/result")
    public ResponseEntity<?> update(@RequestBody DietResult dietResult) {
        try {
            boolean isUpdated = dietResultService.updateDietResult(dietResult);
            if (isUpdated) {
                return new ResponseEntity<>(HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 4. 식단 삭제
    // 특정 메뉴를 식단에서 빼고 싶을 때 사용합니다.
    @Operation(summary = "식단 삭제", description = "특정 식단(mealId)을 삭제합니다.")
    @DeleteMapping("/result/{mealId}")
    public ResponseEntity<?> delete(@PathVariable("mealId") Long mealId) {
        try {
            boolean isDeleted = dietResultService.deleteDietResult(mealId);
            if (isDeleted) {
                return new ResponseEntity<>(HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 5. 식단 수동 추가
    // RAG 추천 외에 사용자가 직접 메뉴를 추가하고 싶을 때 사용합니다.
    @Operation(summary = "식단 수동 추가", description = "사용자가 직접 메뉴를 추가합니다.")
    @PostMapping("/result")
    public ResponseEntity<?> insert(@RequestBody DietResult dietResult) {
        try {
            boolean isInserted = dietResultService.insertDietResult(dietResult);
            if (isInserted) {
                return new ResponseEntity<>(HttpStatus.CREATED);
            }
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}