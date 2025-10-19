package com.influenceflow.model;

import java.time.LocalDateTime;

public class TgUser {
    private long id;
    private long telegramId;
    private String username;
    private boolean admin;
    private LocalDateTime createdAt;

    public TgUser() {
    }

    public TgUser(long id, long telegramId, String username, boolean admin, LocalDateTime createdAt) {
        this.id = id;
        this.telegramId = telegramId;
        this.username = username;
        this.admin = admin;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTelegramId() {
        return telegramId;
    }

    public void setTelegramId(long telegramId) {
        this.telegramId = telegramId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
