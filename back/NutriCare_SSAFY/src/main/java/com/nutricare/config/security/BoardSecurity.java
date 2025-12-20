package com.nutricare.config.security;

import org.springframework.stereotype.Component;
import com.nutricare.model.dto.Board;
import com.nutricare.model.dto.Comment;
import com.nutricare.model.service.BoardService;
import com.nutricare.model.service.CommentService;

@Component("boardSecurity")
public class BoardSecurity {
	
	private final BoardService boardService;
	private final CommentService commentService;
	
	public BoardSecurity(BoardService boardService, CommentService commentService) {
		this.boardService = boardService;
		this.commentService = commentService;
	}
	
	public boolean isBoardOwner(Long boardId, CustomUserDetails userDetails) {
		if (userDetails == null) return false;
		
		if ("ADMIN".equals(userDetails.getUser().getRole())) return true;
		
		Board board = boardService.selectById(boardId);
		if (board == null) return false;
		
		return board.getUserId().equals(userDetails.getUser().getUserId());
	}
	
	public boolean isCommentOwner(Long commentId, CustomUserDetails userDetails) {
		if (userDetails == null) return false;
		
		if (userDetails.getUser().getRole().equals("ADMIN")) return true;
		
		Comment comment = commentService.selectById(commentId);
		
		if (comment == null) return false;
		
		return comment.getUserId().equals(userDetails.getUser().getUserId());
	}
	
}
