package com.nutricare.model.controller;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/board-api")
@Tag(name = "File Upload API", description = "파일 업로드 기능")
public class FileController {

    // 파일을 저장할 로컬 경로 (C드라이브에 폴더를 미리 만들어두세요!)
    // 윈도우 예시: "C:/nutricare_images/"
    // 맥/리눅스 예시: "/Users/name/nutricare_images/"
    private final String uploadDir = "C:/nutricare_images/";

    @Operation(summary = "이미지 파일 업로드", description = "이미지 파일을 업로드하고 접근 가능한 URL을 반환합니다.")
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return new ResponseEntity<>("File is empty", HttpStatus.BAD_REQUEST);
            }

            // 1. 폴더가 없으면 생성
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // 2. 파일명 중복 방지를 위해 UUID 사용 (ex: uuid_originalName.jpg)
            String originalFilename = file.getOriginalFilename();
            String savedFilename = UUID.randomUUID().toString() + "_" + originalFilename;
            String filePath = uploadDir + savedFilename;

            // 3. 파일 저장
            file.transferTo(new File(filePath));

            // 4. 접근 가능한 URL 반환 (예: /images/uuid_image.jpg)
            // 나중에 WebMvcConfig에서 이 URL을 실제 폴더랑 연결해줄 겁니다.
            String fileUrl = "/images/" + savedFilename;
            
            return new ResponseEntity<>(fileUrl, HttpStatus.OK);

        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>("File Upload Failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}