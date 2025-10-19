package com.influenceflow.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Campaign {
    private long id;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal rewardPerSubmission;

    public Campaign() {
    }

    public Campaign(long id, String name, String description, LocalDate startDate, LocalDate endDate,
                     BigDecimal rewardPerSubmission) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.rewardPerSubmission = rewardPerSubmission;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getRewardPerSubmission() {
        return rewardPerSubmission;
    }

    public void setRewardPerSubmission(BigDecimal rewardPerSubmission) {
        this.rewardPerSubmission = rewardPerSubmission;
    }
}
