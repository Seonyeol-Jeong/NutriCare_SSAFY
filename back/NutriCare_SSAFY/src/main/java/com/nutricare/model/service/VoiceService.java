package com.nutricare.model.service;

import org.springframework.web.multipart.MultipartFile;

public interface VoiceService {
	String transcribe(MultipartFile audioFile);
	String parseResponse(String jsonResponse);
}
