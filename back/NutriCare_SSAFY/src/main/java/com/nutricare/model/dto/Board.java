package com.nutricare.model.dto;

import java.time.LocalDateTime;
import java.util.List;

public class Board {

    private Long boardId;          // board.board_id
    private Long userId;           // 작성자 FK
    private String userName;
    private String title;
    private String content;
    private String category;       // 자유/질문/공지 등 문자열로 관리
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<BoardImage> images;
    private boolean deleted;       // is_deleted
    

    // 선택: 조인해서 가져올 경우 작성자 이름까지 담고 싶을 때
    private String authorName;
    

    public Board() {
    }

    public Board(Long boardId, Long userId, String userName, String title, String content, String category, Integer viewCount,
			LocalDateTime createdAt, LocalDateTime updatedAt, List<BoardImage> images, boolean deleted,
			String authorName) {
		super();
		this.boardId = boardId;
		this.userId = userId;
		this.userName = userName;
		this.title = title;
		this.content = content;
		this.category = category;
		this.viewCount = viewCount;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.images = images;
		this.deleted = deleted;
		this.authorName = authorName;
	}

	public Board(String title, String content, String category) {
		this.title = title;
		this.content = content;
		this.category = category;
	}
    
	public Board(String title, String content, String category, List<BoardImage> images) {
		this.title = title;
		this.content = content;
		this.category = category;
		this.images = images;
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
    
    public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
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

	public List<BoardImage> getImages() {
		return images;
	}

	public void setImages(List<BoardImage> images) {
		this.images = images;
	}

	@Override
	public String toString() {
		return "Board [boardId=" + boardId + ", userId=" + userId + ", userName=" + userName + ", title=" + title
				+ ", content=" + content + ", category=" + category + ", viewCount=" + viewCount + ", createdAt="
				+ createdAt + ", updatedAt=" + updatedAt + ", images=" + images + ", deleted=" + deleted
				+ ", authorName=" + authorName + "]";
	}
    
}
