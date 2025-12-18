package com.nutricare.controller;

import com.nutricare.model.service.VoiceService;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/voice")
public class VoiceController {

    private final VoiceService voiceService;

    public VoiceController(VoiceService voiceService) {
        this.voiceService = voiceService;
    }

    @PostMapping(value = "/command", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> handleVoiceCommand(@RequestParam("file") MultipartFile file) {
        // 1. 서비스 호출 (파일 -> 텍스트)
        String commandText = voiceService.transcribe(file);
        
        System.out.println("인식된 음성 명령: " + commandText);

        // 2. 결과 반환 (프론트엔드에서 이 텍스트를 보고 페이지 이동 등을 처리)
        return ResponseEntity.ok(Map.of("text", commandText));
    }
}