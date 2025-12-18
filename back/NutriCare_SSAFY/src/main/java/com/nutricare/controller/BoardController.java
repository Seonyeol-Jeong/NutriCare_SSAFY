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
import org.springframework.web.server.ResponseStatusException;

import com.nutricare.config.security.CustomUserDetails;
import com.nutricare.model.dto.Board;
import com.nutricare.model.service.BoardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/boards")
@Tag(name = "Board RESful API", description = "게시글 CRUD를 할 수 있는 REST API")
public class BoardController {

	private final BoardService boardService;

	@Autowired
	public BoardController(BoardService boardService) {
		this.boardService = boardService;
	}

	// 게시글 전체 목록 조회
	@Operation(summary = "게시글 전체 조회", description = "모든 게시글을 조회합니다.")
	@GetMapping("")
	public ResponseEntity<?> getList() {
		try {
			List<Board> boardList = boardService.selectAll();
			if (boardList != null && !boardList.isEmpty()) {
				return new ResponseEntity<List<Board>>(boardList, HttpStatus.OK);
			}
			return new ResponseEntity<List<Board>>(boardList, HttpStatus.NO_CONTENT);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// 2. 게시글 상세 조회
	@Operation(summary = "게시글 상세 조회", description = "ID에 해당하는 게시글 하나를 조회합니다.")
	@GetMapping("/{boardId}")
	public ResponseEntity<?> getBoard(@PathVariable("boardId") long id) {
		try {
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
	
	@Operation(summary = "나의 게시글 가져오기", description = "로그인한 사용자의 게시글 목록을 가져옵니다.")
	@GetMapping("/me")
	public ResponseEntity<?> getMyBoard(@AuthenticationPrincipal CustomUserDetails userDetails) {
		try {
			Long userId = userDetails.getUser().getUserId();
			List<Board> boardList = boardService.selectListByUserId(userId);
			if (boardList != null) {
				return new ResponseEntity<List<Board>>(boardList, HttpStatus.OK);
			}
			return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// 3. 게시글 등록 (가장 중요! 파일 정보도 여기서 같이 받음)
	@Operation(summary = "게시글 등록", description = "게시글과 이미지 정보를 등록합니다.")
	@PostMapping("")
	public ResponseEntity<?> write(@RequestBody Board board, @AuthenticationPrincipal CustomUserDetails userDetails) {
		// @RequestBody: 프론트에서 보낸 JSON 데이터(images 배열 포함)를 Board 객체로 변환
		try {
			// 작성자 ID를 로그인한 유저 ID로 강제 설정 (보안 강화)
			board.setUserId(userDetails.getUser().getUserId());

			int result = boardService.insert(board);
			if (result > 0) {
				return new ResponseEntity<Board>(board, HttpStatus.CREATED);
			}
			return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			e.printStackTrace(); 
			return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// 4. 게시글 수정
	@Operation(summary = "게시글 수정", description = "게시글 내용을 수정합니다.")
	@PutMapping("/{boardId}")
	public ResponseEntity<?> update(@PathVariable("boardId") long boardId, @RequestBody Board board,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		try {
			checkAuthority(boardId, userDetails);
			
			board.setBoardId(boardId);

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
	@DeleteMapping("/{boardId}")
	public ResponseEntity<?> delete(@PathVariable("boardId") long boardId,
			@AuthenticationPrincipal CustomUserDetails userDetails) { 
		try {
			checkAuthority(boardId, userDetails);

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
	
	private void checkAuthority(Long boardId, CustomUserDetails userDetails) {
        // 1. 게시글 존재 확인
        Board board = boardService.selectById(boardId);
        if (board == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다.");
        }

        // 2. 관리자(ADMIN)는 모든 글 수정/삭제 가능 (프리패스)
        if (userDetails.getUser().getRole().equals("ADMIN")) {
            return;
        }

        // 3. 작성자 본인 확인
        if (!board.getUserId().equals(userDetails.getUser().getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 게시글에 대한 권한이 없습니다.");
        }
    }

}
