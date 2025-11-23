package com.nutricare.model.dao;

import java.util.List;
import com.nutricare.model.dto.Comment;

public interface CommentDao {
	List<Comment> selectAll(int boardId);
	int insert(Comment comment);
	int update(Comment comment);
	int delete(int id);
}
