package com.nutricare.controller;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.nutricare.model.dto.User;
import com.nutricare.model.service.UserService;
import com.nutricare.util.JwtUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@Tag(name = "User API", description = "회원 관련 API (회원가입, 로그인, 내 정보 관리)")	
@RestController
@RequestMapping("/user")
public class UserRestController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    
    // 의존성 주입
    @Autowired
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
    @Operation(
            summary = "내 정보 조회",
            description = "JWT에 포함된 userId를 기반으로 본인의 정보를 조회합니다.<br>"
                        + "**Authorization: Bearer {JWT}** 필수"
    )
    @GetMapping("/me")
    public User getMyInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUser().getUserId();

        // userId 기반으로 DB 조회
        return userService.getUserDetail(userId);
    }

    // 4) 내 정보 수정
    @Operation(
            summary = "내 정보 수정",
            description = "본인의 사용자 정보를 수정합니다.<br>"
                        + "**Authorization: Bearer {JWT}** 필수"
    )
    @PatchMapping("/me")
    public boolean updateMyInfo(@RequestBody User user, @AuthenticationPrincipal CustomUserDetails userDetails) {

        // 1) 토큰 주인(로그인한 사람)의 ID로 강제 세팅 -> 남의 정보 수정 방지
    	Long userId = userDetails.getUser().getUserId();

        // 2) 수정할 User 객체에 userId 강제 세팅
        user.setUserId(userId);

        // 3) 서비스 호출
        return userService.updateUser(user);
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

