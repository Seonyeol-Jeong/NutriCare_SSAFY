package com.nutricare.model.dto;

public class PasswordUpdateRequest {
    private String currentPassword; // 현재 비밀번호 (검증용)
    private String newPassword;     // 바꿀 비밀번호

    public String getCurrentPassword() { return currentPassword; }
    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}