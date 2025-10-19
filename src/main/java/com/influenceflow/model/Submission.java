package com.influenceflow.model;

import java.time.LocalDateTime;

public class Submission {
    private long id;
    private long taskId;
    private long creatorId;
    private String url;
    private SubmissionStatus status;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;

    public Submission() {
    }

    public Submission(long id, long taskId, long creatorId, String url, SubmissionStatus status,
                       LocalDateTime submittedAt, LocalDateTime reviewedAt) {
        this.id = id;
        this.taskId = taskId;
        this.creatorId = creatorId;
        this.url = url;
        this.status = status;
        this.submittedAt = submittedAt;
        this.reviewedAt = reviewedAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(long creatorId) {
        this.creatorId = creatorId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public SubmissionStatus getStatus() {
        return status;
    }

    public void setStatus(SubmissionStatus status) {
        this.status = status;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }
}
