package com.nutricare.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
	/**
     * 1. 권한 없는 접근 처리 (403 Forbidden)
     * SecurityConfig나 @PreAuthorize에서 권한이 막혔을 때 발생합니다.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDeniedException(AccessDeniedException e) {
        // 로그 남기기 (선택)
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("접근 권한이 없습니다.");
    }
    
    /**
     * 2. 잘못된 요청 데이터 처리 (400 Bad Request)
     * Service에서 throw new IllegalArgumentException("...") 한 메시지를 그대로 반환합니다.
     * 예: "비밀번호가 일치하지 않습니다.", "존재하지 않는 게시글입니다." 등
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
    }
    
    /**
     * 3. 그 외 알 수 없는 서버 에러 처리 (500 Internal Server Error)
     * 예상치 못한 에러가 발생했을 때 상세 내용은 숨기고 일반적인 메시지를 보냅니다.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        e.printStackTrace(); // 콘솔에 에러 스택 출력 (실무에서는 로그 파일로 남김)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("서버 내부 오류가 발생했습니다. 관리자에게 문의하세요.");
    }
}
