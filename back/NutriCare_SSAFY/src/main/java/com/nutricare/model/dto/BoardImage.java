package com.nutricare.model.dto;

import java.time.LocalDateTime;

public class BoardImage {

    private Long imageId;        // image_id
    private Long boardId;        // 게시글 FK
    private String imageUrl;     // 파일 URL
    private LocalDateTime createdAt;

    public BoardImage() {}

    public BoardImage(Long imageId, Long boardId, String imageUrl, LocalDateTime createdAt) {
        this.imageId = imageId;
        this.boardId = boardId;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
    }

    public Long getImageId() {
        return imageId;
    }

    public void setImageId(Long imageId) {
        this.imageId = imageId;
    }

    public Long getBoardId() {
        return boardId;
    }

    public void setBoardId(Long boardId) {
        this.boardId = boardId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
