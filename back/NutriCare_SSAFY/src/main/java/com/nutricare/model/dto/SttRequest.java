package com.nutricare.model.dto;

import java.util.Base64;

public class SttRequest {
    private Config config;
    private Audio audio;

    public SttRequest(byte[] audioBytes, int sampleRate) {
        // 오디오 데이터를 Base64 문자열로 변환 (REST API 필수)
        String base64Audio = Base64.getEncoder().encodeToString(audioBytes);
        
        this.audio = new Audio(base64Audio);
        this.config = new Config(sampleRate);
    }

    public Config getConfig() { return config; }
    public Audio getAudio() { return audio; }

    // 내부 클래스: 설정 정보
    public static class Config {
        private String encoding = "LINEAR16"; // WAV 파일인 경우
        private int sampleRateHertz;          // 예: 44100
        private String languageCode = "ko-KR";
        private String model = "command_and_search"; // Short 모델에 해당

        public Config(int sampleRateHertz) {
            this.sampleRateHertz = sampleRateHertz;
        }
        // Getters needed for Jackson to serialize
        public String getEncoding() { return encoding; }
        public int getSampleRateHertz() { return sampleRateHertz; }
        public String getLanguageCode() { return languageCode; }
        public String getModel() { return model; }
    }

    // 내부 클래스: 오디오 데이터
    public static class Audio {
        private String content;
        public Audio(String content) { this.content = content; }
        public String getContent() { return content; }
    }
}