package com.influenceflow.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Task {
    private long id;
    private long campaignId;
    private String title;
    private String brief;
    private String platform;
    private BigDecimal payoutAmount;
    private LocalDate deadline;

    public Task() {
    }

    public Task(long id, long campaignId, String title, String brief, String platform, BigDecimal payoutAmount,
                LocalDate deadline) {
        this.id = id;
        this.campaignId = campaignId;
        this.title = title;
        this.brief = brief;
        this.platform = platform;
        this.payoutAmount = payoutAmount;
        this.deadline = deadline;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(long campaignId) {
        this.campaignId = campaignId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBrief() {
        return brief;
    }

    public void setBrief(String brief) {
        this.brief = brief;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public BigDecimal getPayoutAmount() {
        return payoutAmount;
    }

    public void setPayoutAmount(BigDecimal payoutAmount) {
        this.payoutAmount = payoutAmount;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }
}
