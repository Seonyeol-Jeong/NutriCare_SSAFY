package com.nutricare.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nutricare.config.security.CustomUserDetails;
import com.nutricare.model.dto.LoginResponse;
import com.nutricare.model.dto.PasswordUpdateRequest;
import com.nutricare.model.dto.User;
import com.nutricare.model.dto.UserDetailResponse;
import com.nutricare.model.service.UserService;
import com.nutricare.util.JwtUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "User API", description = "회원 관련 API (회원가입, 로그인, 내 정보 관리)")	
@RestController
@RequestMapping("/user")
public class UserRestController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    
    // 의존성 주입
    public UserRestController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    
    // 1) 회원가입
    @Operation(
            summary = "회원가입",
            description = "회원 정보를 입력받아 신규 사용자를 생성합니다. 권한(role)은 기본적으로 'USER'로 설정됩니다."
    )
    @PostMapping
    public boolean register(@RequestBody User user) {
        return userService.registerUser(user);
    }

    // 2) 로그인 (JWT 발급)
    @Operation(
            summary = "로그인",
            description = "이메일과 비밀번호를 검증하고 JWT를 발급합니다.<br> "
                        + "발급된 JWT는 Authorization 헤더에 Bearer {token} 형태로 포함하여 요청해야 합니다."
    )
    @PostMapping("/login")
    public LoginResponse login(@RequestParam String email,
                               @RequestParam String password) {

        User user = userService.login(email, password);
        if (user == null) return null;

        String token = jwtUtil.generateToken(user.getUserId(), user.getRole());

        return new LoginResponse(token, user.getUserId());
    }

 // 3) 내 정보 조회 
    @Operation(summary = "내 정보 조회 (통합)", description = "회원 기본 정보와 건강 프로필을 함께 반환합니다.")
    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Long userId = userDetails.getUser().getUserId();
            
            // ★ 서비스에서 조합된 데이터 가져오기
            UserDetailResponse response = userService.getUserWithProfile(userId);
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 4-1) 회원정보 수정 (이름, 생년, 성별 등)
    @Operation(summary = "내 정보 수정", description = "비밀번호를 제외한 회원 정보를 수정합니다.")
    @PatchMapping("/me/info")
    public ResponseEntity<?> updateMyInfo(@RequestBody User user, 
                                          @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getUserId();
        user.setUserId(userId); // 토큰 ID로 강제 세팅

        boolean result = userService.updateUserInfo(user);
        if (result) return ResponseEntity.ok("정보 수정 성공");
        else return ResponseEntity.badRequest().build();
    }
    
 // 4-2) 비밀번호 변경
    @Operation(summary = "비밀번호 변경", description = "현재 비밀번호 확인 후 새 비밀번호로 변경합니다.")
    @PatchMapping("/me/password")
    public ResponseEntity<?> updatePassword(@RequestBody PasswordUpdateRequest request,
                                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getUserId();
        
        try {
            boolean result = userService.updatePassword(userId, request.getCurrentPassword(), request.getNewPassword());
            if (result) return ResponseEntity.ok("비밀번호 변경 성공");
            else return ResponseEntity.badRequest().body("비밀번호 변경 실패");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    // 5) 내 정보 삭제
    @Operation(
            summary = "회원탈퇴",
            description = "soft delete 방식으로 is_deleted = 1 로 처리합니다.<br>"
                        + "**Authorization: Bearer {JWT}** 필수"
    )
    @DeleteMapping("/me")
    public boolean deleteMyAccount(@AuthenticationPrincipal CustomUserDetails userDetails) {

        // 1) 인터셉터에서 넣어준 userId 가져오기
    	Long userId = userDetails.getUser().getUserId();

        // 2) 서비스 호출
        return userService.deleteUser(userId);
    }

    // 6) 로그아웃
    @Operation(
            summary = "로그아웃",
            description = "JWT 기반 인증 시스템에서는 서버 로그아웃 처리가 없습니다.<br>"
                        + "프론트엔드에서 토큰 삭제 방식으로 처리합니다."
    )
    @PostMapping("/logout")
    public String logout() {
        return "logout success";
    }
}

