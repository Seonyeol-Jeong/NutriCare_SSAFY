package com.nutricare.model.service;

import java.util.List;

import com.nutricare.model.dto.Comment;

public interface CommentService {
	List<Comment> selectAll(long boardId);
	int insert(Comment comment);
	int update(Comment comment);
	int delete(long commentId);
}
