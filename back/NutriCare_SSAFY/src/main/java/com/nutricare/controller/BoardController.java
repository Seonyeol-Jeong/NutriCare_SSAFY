package com.nutricare.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.nutricare.model.dto.Board;
import com.nutricare.model.service.BoardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/board-api")
@Tag(name = "Board RESful API", description = "게시글 CRUD를 할 수 있는 REST API")
public class BoardController {

	private final BoardService boardService;

	@Autowired
	public BoardController(BoardService boardService) {
		this.boardService = boardService;
	}

	// 게시글 전체 목록 조회
	@Operation(summary = "게시글 전체 조회", description = "모든 게시글을 조회합니다.")
	@GetMapping("/board")
	public ResponseEntity<?> getList() {
		try {
			List<Board> boardList = boardService.selectAll();
			if (boardList != null && !boardList.isEmpty()) {
				return new ResponseEntity<List<Board>>(boardList, HttpStatus.OK);
			}
			return new ResponseEntity<List<Board>>(boardList, HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// 2. 게시글 상세 조회
	@Operation(summary = "게시글 상세 조회", description = "ID에 해당하는 게시글 하나를 조회합니다.")
	@GetMapping("/board/{boardId}")
	public ResponseEntity<?> getBoard(@PathVariable("boardId") long id) {
		try {
			// 조회수 증가를 먼저 시키거나, 서비스 내부에서 처리할 수 있습니다.
			boardService.updateViewCnt(id);

			Board board = boardService.selectById(id);
			if (board != null) {
				return new ResponseEntity<Board>(board, HttpStatus.OK);
			}
			return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// 3. 게시글 등록 (가장 중요! 파일 정보도 여기서 같이 받음)
	@Operation(summary = "게시글 등록", description = "게시글과 이미지 정보를 등록합니다.")
	@PostMapping("/board")
	public ResponseEntity<?> write(@RequestBody Board board, @AuthenticationPrincipal CustomUserDetails userDetails) {
		// @RequestBody: 프론트에서 보낸 JSON 데이터(images 배열 포함)를 Board 객체로 변환
		try {
			// 작성자 ID를 로그인한 유저 ID로 강제 설정 (보안 강화)
			board.setUserId(userDetails.getUser().getUserId());

			int result = boardService.insert(board);
			if (result > 0) {
				// 201 Created 상태코드 반환
				return new ResponseEntity<Integer>(result, HttpStatus.CREATED);
			}
			return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			e.printStackTrace(); // 에러 로그 확인용
			return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// 4. 게시글 수정
	@Operation(summary = "게시글 수정", description = "게시글 내용을 수정합니다.")
	@PutMapping("/board/{boardId}")
	public ResponseEntity<?> update(@PathVariable("boardId") long boardId, @RequestBody Board board,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		try {
			Board origin = boardService.selectById(boardId);
			if (origin == null) {
				return new ResponseEntity<Void>(HttpStatus.NOT_FOUND); // 404: 글이 없음
			}

			// 작성자 본인 확인 (DB에 저장된 작성자 ID vs 현재 로그인한 유저 ID)
			// (관리자 권한(ADMIN)이 있으면 통과시켜주는 로직을 넣을 수도 있습니다)
			Long currentUserId = userDetails.getUser().getUserId();
			if (!origin.getUserId().equals(currentUserId)) {
				return new ResponseEntity<Void>(HttpStatus.FORBIDDEN); // 403: 권한 없음 (남의 글임)
			}

			// 본인이 맞으면 수정 진행
			board.setBoardId(boardId);
			// (선택) 작성자 ID 변조 방지를 위해 기존 ID로 덮어쓰거나, Mapper에서 수정하지 않도록 해야 함
			board.setUserId(currentUserId);

			int result = boardService.update(board);
			if (result > 0) {
				return new ResponseEntity<Integer>(result, HttpStatus.OK);
			}
			return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// 5. 게시글 삭제 (보안 강화)
	@Operation(summary = "게시글 삭제", description = "ID에 해당하는 게시글을 삭제합니다. (작성자 본인만 가능)")
	@DeleteMapping("/board/{boardId}")
	public ResponseEntity<?> delete(@PathVariable("boardId") long boardId,
			@AuthenticationPrincipal CustomUserDetails userDetails) { // ★ 1. 유저 정보 주입
		try {
			// ★ 2. 삭제하려는 글 조회
			Board origin = boardService.selectById(boardId);
			if (origin == null) {
				return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
			}

			// ★ 3. 작성자 본인 확인
			Long currentUserId = userDetails.getUser().getUserId();
			if (!origin.getUserId().equals(currentUserId)) {
				return new ResponseEntity<Void>(HttpStatus.FORBIDDEN); // 403: 넌 못 지워!
			}

			// 4. 본인이 맞으면 삭제 진행
			int result = boardService.delete(boardId);
			if (result > 0) {
				return new ResponseEntity<Integer>(result, HttpStatus.OK);
			}
			return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
