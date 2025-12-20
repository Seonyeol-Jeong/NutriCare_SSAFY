package com.nutricare.model.service;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
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
    public Comment selectById(long commentId) {
        return commentDao.selectById(commentId);
    }

	@Override
	public int insert(Comment comment) {
		return commentDao.insert(comment);
	}

	@Override
	@PreAuthorize("@boardSecurity.isCommentOwner(#comment.commentId, principal)")
	public int update(Comment comment) {
		return commentDao.update(comment);
	}

	@Override
	@PreAuthorize("@boardSecurity.isCommentOwner(#commentId, principal)")
	public int delete(long commentId) {
		return commentDao.delete(commentId);
	}

}
