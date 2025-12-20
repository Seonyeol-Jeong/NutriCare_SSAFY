package com.nutricare.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nutricare.config.security.CustomUserDetails;
import com.nutricare.model.dto.Comment;
import com.nutricare.model.service.CommentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api")
@Tag(name = "Comment RESful API", description = "댓글 CRUD를 제공하는 REST API")
public class CommentController {
	private final CommentService commentService;

	public CommentController(CommentService commentService) {
		this.commentService = commentService;
	}

	// 특정 게시글의 댓글 목록 조회
	@Operation(summary = "특정 게시글의 댓글 목록 조회")
	@GetMapping("/boards/{boardId}/comments")
	public ResponseEntity<?> list(@PathVariable("boardId") Long boardId) {
		try {
			return ResponseEntity.ok(commentService.selectAll(boardId.intValue()));
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// 댓글 작성
	@Operation(summary = "댓글 작성")
	@PostMapping("/boards/{boardId}/comments")
	public ResponseEntity<?> insert(@PathVariable("boardId") Long boardId, @RequestBody Comment comment,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		try {
			comment.setBoardId(boardId);
			comment.setUserId(userDetails.getUser().getUserId());
			int result = commentService.insert(comment);
			if (result > 0) {
				return ResponseEntity.status(HttpStatus.CREATED).body("comment write success");
			}
			return ResponseEntity.badRequest().body("comment writing failed");
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().body("server Error");
		}
	}

	// 댓글 수정
	@Operation(summary = "댓글 수정")
	@PutMapping("/comments/{commentId}")
	public ResponseEntity<?> update(@PathVariable("commentId") Long commentId, @RequestBody Comment comment,
			@AuthenticationPrincipal CustomUserDetails userDetails) { // ★ 유저 정보
		try {

			comment.setCommentId(commentId);
			int result = commentService.update(comment);
			if (result > 0) {
				return ResponseEntity.ok("comment updated succesfully");
			}
			return ResponseEntity.badRequest().body("comment updating failed");
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().body("serverError");
		}
	}

	// 댓글 삭제 (soft delete)
	@Operation(summary = "댓글 삭제 (소프트 삭제)")
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<?> delete(@PathVariable("commentId") Long commentId,
                                    @AuthenticationPrincipal CustomUserDetails userDetails) { // ★ 유저 정보
        try {
            // ★ 1. 댓글 존재 여부 및 작성자 확인
            Comment origin = commentService.selectById(commentId);
            if (origin == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("comment not found");
            }

            // ★ 2. 본인 확인
            if (!origin.getUserId().equals(userDetails.getUser().getUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not the author");
            }

            int result = commentService.delete(commentId.intValue());
            if (result > 0) {
                return ResponseEntity.ok("comment deleted successfully");
            }
            return ResponseEntity.badRequest().body("comment deleting failed");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("server Error");
        }
    }
}
