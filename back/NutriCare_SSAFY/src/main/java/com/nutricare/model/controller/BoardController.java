package com.nutricare.model.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nutricare.model.dto.Board;
import com.nutricare.model.service.BoardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/board-api")
@Tag(name = "Board RESful API", description = "게시글 CRUD를 할 수 있는 REST API")
public class BoardController {

	private final BoardService boardService;

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
	@GetMapping("/board/{id}")
	public ResponseEntity<?> getBoard(@PathVariable long id) {
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
	public ResponseEntity<?> write(@RequestBody Board board) {
		// @RequestBody: 프론트에서 보낸 JSON 데이터(images 배열 포함)를 Board 객체로 변환
		try {
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
	@PutMapping("/board/{id}")
	public ResponseEntity<?> update(@PathVariable("id") long id, @RequestBody Board board) {
		try {
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

	// 5. 게시글 삭제
	@Operation(summary = "게시글 삭제", description = "ID에 해당하는 게시글을 삭제합니다.")
	@DeleteMapping("/board/{id}")
	public ResponseEntity<?> delete(@PathVariable long id) {
		try {
			int result = boardService.delete(id);
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
