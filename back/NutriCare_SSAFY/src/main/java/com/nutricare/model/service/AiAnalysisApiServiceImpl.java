package com.nutricare.model.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutricare.config.security.CustomUserDetails;

@Service
public class AiAnalysisApiServiceImpl implements AiAnalysisApiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // FastAPI URL (.env로 관리)
    private final String aiUrl;
    private final Logger log = LoggerFactory.getLogger(getClass());
    public AiAnalysisApiServiceImpl(ObjectMapper objectMapper,
                                    @Value("${ai.fastapi.url}") String aiUrl) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
        this.aiUrl = aiUrl;
    }

    @Override
    @PreAuthorize("@dietSecurity.isPhotoOwner(#photoId, principal)")
    public String requestAnalysis(Long photoId, String photoUrl) {
    	try {
    		
    		CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    		Long currentUserId = userDetails.getUser().getUserId();
            // 1. Request Body & Header (기존 동일)
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("photo_id", photoId);
            requestBody.put("user_id", currentUserId);
            requestBody.put("photo_url", photoUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // 2. 요청 전송
            Map<String, Object> response = restTemplate.postForObject(aiUrl, entity, Map.class);

            // 3. 결과 파싱 (Null 체크 강화)
            if (response == null || !response.containsKey("diagnosis_name")) {
                log.warn("AI 분석 결과 형식이 올바르지 않음: response={}", response);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "AI 분석 결과가 비어있습니다.");
            }

            return (String) response.get("diagnosis_name");

        } catch (HttpClientErrorException e) {
            // 4xx 에러: 우리가 보낸 데이터가 잘못됨
            log.error("AI 요청 클라이언트 오류 (4xx): status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "AI 분석 요청 데이터가 올바르지 않습니다.");

        } catch (HttpServerErrorException e) {
            // 5xx 에러: AI 서버 내부 문제
            log.error("AI 서버 오류 (5xx): status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "AI 서비스가 일시적으로 불가능합니다.");

        } catch (ResourceAccessException e) {
            // 타임아웃 또는 연결 거부 (AI 서버 다운)
            log.error("AI 서버 연결 실패: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "AI 서버 응답 시간이 초과되었습니다.");

        } catch (Exception e) {
            // 그 외 알 수 없는 오류
            log.error("AI 분석 중 알 수 없는 오류 발생", e);
            throw new RuntimeException("시스템 내부 오류로 분석에 실패했습니다.", e);
        }
    }
}