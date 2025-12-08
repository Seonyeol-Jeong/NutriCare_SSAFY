package com.nutricare.model.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nutricare.model.dto.RagDietContext;

@Service
public class RagApiServiceImpl implements RagApiService {

    // FastAPI 서버에 HTTP 요청을 보내기 위한 Spring 기본 HTTP 클라이언트
    private final RestTemplate restTemplate;

    // Java 객체 <-> JSON 변환을 위한 Jackson ObjectMapper
    private final ObjectMapper objectMapper;

    // FastAPI RAG 엔드포인트 URL
    private final String ragUrl = "http://fastapi-server:8000/rag/diet";

    /**
     * 기본 생성자
     * - RestTemplate 초기화
     * - ObjectMapper 생성 + LocalDateTime 등 직렬화를 위한 JavaTimeModule 등록
     */
    public RagApiServiceImpl() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * FastAPI 서버로 식단 RAG 요청을 보내는 메서드
     *
     * @param context  → RAG에 필요한 사용자 정보(진단 결과, 건강 정보 등)
     * @return FastAPI의 응답(JSON 문자열)
     */
    @Override
    public String requestDietGeneration(RagDietContext context) {
        try {
            // 1) Java 객체를 JSON 문자열로 변환
            String json = objectMapper.writeValueAsString(context);

            // 2) 헤더 설정: JSON 형식으로 보낸다는 의미
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 3) HttpEntity 생성 (JSON + 헤더 묶음)
            HttpEntity<String> entity = new HttpEntity<>(json, headers);

            // 4) FastAPI 서버로 POST 요청 전송
            // - body : context JSON
            // - return type : String
            String response = restTemplate.postForObject(ragUrl, entity, String.class);

            // 5) FastAPI 응답 문자열 반환
            return response;

        } catch (JsonProcessingException e) {
            // 직렬화 실패 시 런타임 예외 던짐
            throw new RuntimeException("Failed to serialize RagDietContext", e);
        }
    }
}
