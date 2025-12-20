package com.nutricare.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nutricare.model.dto.User;
import com.nutricare.model.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Admin API", description = "관리자 전용 API (전체 회원 관리)")
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserRestController {

	@Autowired
	private UserService userService;

	
	@Operation(
	        summary = "전체 회원 조회",
	        description = """
	                      전체 회원 리스트를 조회합니다.<br>
	                      이 API는 관리자(Admin Role)만 접근할 수 있습니다.<br><br>
	                      **Authorization: Bearer {JWT} 필수**  
	                      JWT 내부 Claims.role 값이 'ADMIN' 이어야 접근 가능
	                      """
	    )
	@GetMapping("/users")
	public List<User> getAll() {
		return userService.getAllUsers();
	}
}