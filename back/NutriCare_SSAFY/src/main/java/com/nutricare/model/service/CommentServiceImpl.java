package com.nutricare.model.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nutricare.model.dao.CommentDao;
import com.nutricare.model.dto.Comment;

@Service
@Transactional
public class CommentServiceImpl implements CommentService{

	private final CommentDao commentDao;

	public CommentServiceImpl(CommentDao commentDao) {
		this.commentDao = commentDao;
	}
	
	@Override
	public List<Comment> selectAll(long boardId) {
		return commentDao.selectAll(boardId);
	}

	@Override
	public int insert(Comment comment) {
		return commentDao.insert(comment);
	}

	@Override
	public int update(Comment comment) {
		return commentDao.update(comment);
	}

	@Override
	public int delete(long commentId) {
		return commentDao.delete(commentId);
	}

}
