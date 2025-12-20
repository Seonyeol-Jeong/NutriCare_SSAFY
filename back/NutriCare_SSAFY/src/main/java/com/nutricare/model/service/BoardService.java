package com.nutricare.model.service;

import java.util.List;

import com.nutricare.model.dto.Board;

public interface BoardService {
	//게시글 전체 목록 조회
	List<Board> selectAll();
	
	//게시글 ID로 상세 조회
	Board selectById (long id);
	
	//유저 ID로 게시글 목록 조회
	List<Board> selectListByUserId(Long userId);
	
	//게시글 등록
	int insert(Board board);
	
	//게시글 수정
	int update(Board board);
	
	//id로 게시글 삭제
	int delete(long boardId);
	
	//조회수 증가
	int updateViewCnt(long boardId);
	
	//파일 등록
	public int insertBoardImages(Board board);
}
