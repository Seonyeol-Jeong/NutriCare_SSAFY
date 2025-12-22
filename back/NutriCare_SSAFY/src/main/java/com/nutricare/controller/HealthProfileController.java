package com.nutricare.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nutricare.config.security.CustomUserDetails;
import com.nutricare.model.dto.HealthProfile;
import com.nutricare.model.service.HealthProfileService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/health-profiles")
@Tag(name = "HealthProfile API", description = "사용자 건강 정보(키, 몸무게 등) 관리 API")
public class HealthProfileController {

    private final HealthProfileService healthProfileService;

    public HealthProfileController(HealthProfileService healthProfileService) {
        this.healthProfileService = healthProfileService;
    }

    @Operation(summary = "내 건강 정보 조회", description = "로그인한 사용자의 건강 정보를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Long userId = userDetails.getUser().getUserId();
            HealthProfile profile = healthProfileService.getHealthProfile(userId);
            
            if (profile != null) {
                return new ResponseEntity<>(profile, HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 정보 없음 (204)
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "건강 정보 등록 및 수정", description = "건강 정보를 등록합니다. 이미 존재하면 수정합니다.")
    @PostMapping("/me")
    public ResponseEntity<?> saveProfile(@RequestBody HealthProfile healthProfile,
                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            // 토큰의 userId로 강제 설정 (보안)
            Long userId = userDetails.getUser().getUserId();
            healthProfile.setUserId(userId);

            boolean result = healthProfileService.saveOrUpdateHealthProfile(healthProfile);
            
            if (result) {
                return new ResponseEntity<>(HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}