package com.nutricare.model.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutricare.model.dao.DietResultDao;
import com.nutricare.model.dto.DietResult;

@Service
public class DietResultServiceImpl implements DietResultService {

    private final DietResultDao dietResultDao;
    private final ObjectMapper objectMapper; // JSON 파싱용

    public DietResultServiceImpl(DietResultDao dietResultDao, ObjectMapper objectMapper) {
        this.dietResultDao = dietResultDao;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public boolean insertDietResult(DietResult dietResult) {
        return dietResultDao.insertDietResult(dietResult) > 0;
    }

    @Override
    public List<DietResult> getDietResultsByRecId(Long recId) {
        return dietResultDao.selectByRecId(recId);
    }

    @Override
    public DietResult getDietResultById(Long mealId) {
        return dietResultDao.selectByMealId(mealId);
    }

    @Override
    @Transactional
    public boolean updateDietResult(DietResult dietResult) {
        return dietResultDao.updateDietResult(dietResult) > 0;
    }

    @Override
    @Transactional
    public boolean deleteDietResult(Long mealId) {
        return dietResultDao.deleteDietResult(mealId) > 0;
    }

    /**
     * RAG 모델의 JSON 응답을 파싱하여 DB에 저장합니다.
     * 가정: RAG가 `[ { "menuName": "...", "calories": 500, ... }, ... ]` 형태의 JSON 배열을 반환한다고 가정
     */
    @Override
    @Transactional
    public void saveDietResultsFromJson(Long recId, String jsonResponse) {
        try {
            // 1. JSON 문자열 전처리
            String cleanJson = cleanJson(jsonResponse);

            // 2. 파싱 (예외 발생 가능 지점)
            List<DietResult> results = objectMapper.readValue(cleanJson, new TypeReference<List<DietResult>>() {});

            // 3. DB 저장
            for (DietResult result : results) {
                result.setRecId(recId);
                // [반영] dao 직접 호출 대신, 위에서 만든 메서드 재사용
                // (내부 호출이라 @Transactional 전파는 안 되지만, 이 메서드 자체가 Transactional이라 괜찮음)
                insertDietResult(result);
            }

        } catch (JsonProcessingException e) {
            // [반영] JSON 파싱 에러를 명확한 RuntimeException으로 감싸서 던짐
            e.printStackTrace();
            throw new IllegalArgumentException("RAG 응답 JSON 파싱 실패: 형식이 올바르지 않습니다.", e);
        } catch (Exception e) {
            // 그 외 DB 오류 등
            e.printStackTrace();
            throw new RuntimeException("식단 결과 저장 중 알 수 없는 오류 발생", e);
        }
    }

    // JSON 문자열 전처리 (필요 시 RAG 응답 특성에 맞춰 수정)
    private String cleanJson(String response) {
        if (response == null) return "[]";
        String trimmed = response.trim();
        // 마크다운 코드 블록 제거
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7);
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3);
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3);
        }
        return trimmed.trim();
    }
    
    @Override
    @Transactional
    public boolean deleteDietResultsByRecId(Long recId) {
        return dietResultDao.deleteByRecId(recId) > 0;
    }
}