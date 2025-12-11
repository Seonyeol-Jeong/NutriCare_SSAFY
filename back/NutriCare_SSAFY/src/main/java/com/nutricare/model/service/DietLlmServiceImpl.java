package com.nutricare.model.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nutricare.model.dto.DietContext;
import com.nutricare.model.service.CalorieCalculator.CaloriePlan;

@Service
public class DietLlmServiceImpl implements DietLlmService {

    // FastAPI 서버로 HTTP 요청을 보내기 위한 Spring 기본 HTTP 클라이언트
    private final RestTemplate restTemplate;

    // Java 객체 <-> JSON 변환을 위한 Jackson ObjectMapper
    private final ObjectMapper objectMapper;

    // FastAPI 식단 생성 엔드포인트 URL (properties/환경변수에서 주입)
    private final String dietLlmUrl;

    /**
     * 기본 생성자
     * - RestTemplate 초기화
     * - ObjectMapper 구성 + LocalDateTime 직렬화를 위한 JavaTimeModule 등록
     */
    public DietLlmServiceImpl(@Value("${ai.diet.url:http://localhost:8000/diet/generate}") String dietLlmUrl) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.dietLlmUrl = dietLlmUrl;
    }

    /**
     * FastAPI 서버로 식단 생성 요청을 보내는 메서드
     *
     * @param context 식단 생성에 사용할 컨텍스트(진단 결과, 건강 정보 등)
     * @param plan    칼로리 플랜(BMR/TDEE/목표 kcal 등)
     * @return FastAPI 응답(JSON 문자열)
     */
    @Override
    public String requestDietGeneration(DietContext context, CaloriePlan plan) {
        try {
            // 1) 요청 페이로드 구성
            var payload = new java.util.HashMap<String, Object>();
            payload.put("context", context);
            payload.put("caloriePlan", plan);

            // 2) Java 객체 -> JSON 문자열 변환
            String json = objectMapper.writeValueAsString(payload);

            // 3) 헤더 설정: JSON 포맷으로 보냄
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 4) HttpEntity 생성 (JSON + 헤더 묶음)
            HttpEntity<String> entity = new HttpEntity<>(json, headers);

            // 5) FastAPI 서버에 POST 요청 전송
            // - body : context JSON
            // - return type : String
            String response = restTemplate.postForObject(dietLlmUrl, entity, String.class);

            // 6) FastAPI 응답 문자열 반환
            return response;

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize DietContext", e);
        }
    }
}
