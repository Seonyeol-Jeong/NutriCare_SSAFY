package com.nutricare.model.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AiAnalysisApiServiceImpl implements AiAnalysisApiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // FastAPI URL (환경변수나 properties로 관리하는 것이 좋음)
    private final String aiUrl = "http://fastapi-server:8000/analyze"; 

    public AiAnalysisApiServiceImpl(ObjectMapper objectMapper) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
    }

    @Override
    public String requestAnalysis(Long photoId, Long userId, String photoUrl) {
        try {
            // 1. Request Body 생성
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("photo_id", photoId);
            requestBody.put("user_id", userId);
            requestBody.put("photo_url", photoUrl);

            // 2. Header 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 3. Entity 생성
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // 4. POST 요청 전송
            // 응답 예시: {"analysis_id": null, "photo_id": 1, "diagnosis_name": "아토피"}
            Map<String, Object> response = restTemplate.postForObject(aiUrl, entity, Map.class);

            // 5. 결과 파싱
            if (response != null && response.containsKey("diagnosis_name")) {
                return (String) response.get("diagnosis_name");
            }
            return null;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("AI 분석 요청 실패", e);
        }
    }
}