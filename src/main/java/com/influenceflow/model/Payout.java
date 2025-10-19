package com.influenceflow.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Payout {
    private long id;
    private long creatorId;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private String status;

    public Payout() {
    }

    public Payout(long id, long creatorId, BigDecimal totalAmount, LocalDateTime createdAt, String status) {
        this.id = id;
        this.creatorId = creatorId;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(long creatorId) {
        this.creatorId = creatorId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
