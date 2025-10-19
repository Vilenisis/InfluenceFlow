package com.influenceflow.model;

public class Creator {
    private long id;
    private long tgUserId;
    private String fullName;
    private String email;
    private String niche;
    private String platformHandle;

    public Creator() {
    }

    public Creator(long id, long tgUserId, String fullName, String email, String niche, String platformHandle) {
        this.id = id;
        this.tgUserId = tgUserId;
        this.fullName = fullName;
        this.email = email;
        this.niche = niche;
        this.platformHandle = platformHandle;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTgUserId() {
        return tgUserId;
    }

    public void setTgUserId(long tgUserId) {
        this.tgUserId = tgUserId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNiche() {
        return niche;
    }

    public void setNiche(String niche) {
        this.niche = niche;
    }

    public String getPlatformHandle() {
        return platformHandle;
    }

    public void setPlatformHandle(String platformHandle) {
        this.platformHandle = platformHandle;
    }
}
