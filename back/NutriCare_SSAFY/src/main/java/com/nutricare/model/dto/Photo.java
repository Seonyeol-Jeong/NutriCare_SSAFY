package com.nutricare.model.dto;

import java.time.LocalDateTime;

public class Photo {
    private Long photoId;
    private Long userId;
    private String photoUrl;
    private LocalDateTime createdAt;

    public Photo() {
    }

    public Photo(Long photoId, Long userId, String photoUrl, LocalDateTime createdAt) {
        this.photoId = photoId;
        this.userId = userId;
        this.photoUrl = photoUrl;
        this.createdAt = createdAt;
    }

    public Photo(Long userId, String photoUrl) {
        this.userId = userId;
        this.photoUrl = photoUrl;
    }

    public Long getPhotoId() {
        return photoId;
    }

    public void setPhotoId(Long photoId) {
        this.photoId = photoId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Photo{" +
                "photoId=" + photoId +
                ", userId=" + userId +
                ", photoUrl='" + photoUrl + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
