package com.nutricare.model.service;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nutricare.model.dao.BoardDao;
import com.nutricare.model.dto.Board;

@Service
public class BoardServiceImpl implements BoardService {

	private final BoardDao boardDao;
	
	public BoardServiceImpl(BoardDao boardDao) {
		this.boardDao = boardDao;
	}
	
	@Override
	public List<Board> selectAll() {
		return  boardDao.selectAll();
	}

	@Transactional
	@Override
	public Board selectById(long id) {
		updateViewCnt(id);
		return boardDao.selectById(id);
	}
	
	@Transactional
	@Override
	public List<Board> selectListByUserId(Long userId) {
		return boardDao.selectListByUserId(userId);
	}

	
	//파일 등록을 위한 트랜잭셔널
	@Transactional
	@Override
	public int insert(Board board) {
		int result = boardDao.insert(board);
		
		// board.getImages()가 null이 아니고 비어있지 않을 때만 실행
		if (board.getImages() != null && !board.getImages().isEmpty()) {
			boardDao.insertBoardImages(board);
		}
		
		return result;
	}

	@Override
	@PreAuthorize("@boardSecurity.isBoardOwner(#board.boardId, principal)")
	public int update(Board board) {
		return boardDao.update(board);
	}

	@Override
	@PreAuthorize("@boardSecurity.isBoardOwner(#boardId, principal)")
	public int delete(long boardId) {
		return boardDao.delete(boardId);
	}

	@Override
	public int updateViewCnt(long id) {
		return boardDao.updateViewCnt(id);
	}

	@Override
	@PreAuthorize("@boardSecurity.isBoardOwner(#board.boardId, principal)")
	public int insertBoardImages(Board board) {
		return boardDao.insertBoardImages(board);
	}
}
