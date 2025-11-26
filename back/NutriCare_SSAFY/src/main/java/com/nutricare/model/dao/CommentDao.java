package com.nutricare.model.dao;

import java.util.List;
import com.nutricare.model.dto.Comment;

public interface CommentDao {
	List<Comment> selectAll(long boardId);
	int insert(Comment comment);
	int update(Comment comment);
	int delete(long commentId);
}
