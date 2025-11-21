package com.nutricare.model.dto;

import java.time.LocalDateTime;

public class Comment {

    private Long commentId;        // comment.comment_id
    private Long boardId;          // FK -> board.board_id
    private Long userId;           // 작성자 FK
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean deleted;       // is_deleted

    // 선택: 작성자 이름
    private String authorName;

    public Comment() {
    }

    public Comment(Long commentId, Long boardId, Long userId,
                      String content,
                      LocalDateTime createdAt, LocalDateTime updatedAt,
                      boolean deleted, String authorName) {
        this.commentId = commentId;
        this.boardId = boardId;
        this.userId = userId;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deleted = deleted;
        this.authorName = authorName;
    }
    
    

    public Comment(String content) {
		this.content = content;
	}

	public Long getCommentId() {
        return commentId;
    }

    public void setCommentId(Long commentId) {
        this.commentId = commentId;
    }

    public Long getBoardId() {
        return boardId;
    }

    public void setBoardId(Long boardId) {
        this.boardId = boardId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

	@Override
	public String toString() {
		return "Comment [commentId=" + commentId + ", boardId=" + boardId + ", userId=" + userId + ", content="
				+ content + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + ", deleted=" + deleted
				+ ", authorName=" + authorName + "]";
	}
    
    
}
