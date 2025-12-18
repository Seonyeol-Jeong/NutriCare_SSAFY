package com.nutricare.model.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutricare.model.dto.SttRequest;

import java.io.IOException;

@Service
public class VoiceServiceImpl implements VoiceService {

    @Value("${STT_API_KEY}")
    private String apiKey;

    @Value("${STT_URL}")
    private String sttUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String transcribe(MultipartFile audioFile) {
        try {
            // 1. 요청 URL 완성 (?key=API_KEY)
            String requestUrl = sttUrl + "?key=" + apiKey;

            // 2. 요청 Body 생성 (오디오 파일 -> Base64 -> DTO)
            // 주의: 클라이언트가 보내는 파일의 Sample Rate와 일치해야 합니다. (보통 44100 or 48000)
            SttRequest requestBody = new SttRequest(audioFile.getBytes(), 48000); 

            // 3. Header 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 4. HTTP 요청 전송
            HttpEntity<SttRequest> entity = new HttpEntity<>(requestBody, headers);
            String response = restTemplate.postForObject(requestUrl, entity, String.class);

            // 5. 응답 파싱 (JSON -> 텍스트 추출)
            return parseResponse(response);

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("오디오 파일 처리 실패", e);
        } catch (Exception e) {
            e.printStackTrace();
            return "음성 인식 실패";
        }
    }

    public String parseResponse(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            // 구글 응답 구조: results[0].alternatives[0].transcript
            if (root.has("results")) {
                JsonNode results = root.path("results");
                if (results.isArray() && results.size() > 0) {
                    JsonNode alternatives = results.get(0).path("alternatives");
                    if (alternatives.isArray() && alternatives.size() > 0) {
                        return alternatives.get(0).path("transcript").asText();
                    }
                }
            }
            return ""; // 인식된 결과 없음
        } catch (Exception e) {
            return "";
        }
    }
}