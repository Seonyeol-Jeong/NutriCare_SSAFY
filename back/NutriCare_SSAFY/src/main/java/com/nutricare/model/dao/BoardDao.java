package com.nutricare.model.dao;

import java.util.List;

import com.nutricare.model.dto.Board;

public interface BoardDao {
	//게시글 전체 목록 조회
	List<Board> selectAll();
	
	//게시글 ID별 상세 조회
	Board selectById (long boardId);
	
	//게시글 등록
	int insert(Board board);
	
	//게시글 수정
	int update(Board board);
	
	//id로 게시글 삭제
	int delete(long boardId);
	
	//조회수 증가
	int updateViewCnt(long boardId);
	
	//이미지 등록
	public int insertBoardImages(Board board);
	
	
}
