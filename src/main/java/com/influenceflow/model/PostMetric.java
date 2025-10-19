package com.influenceflow.model;

import java.time.LocalDateTime;

public class PostMetric {
    private long id;
    private long submissionId;
    private int views;
    private int likes;
    private int comments;
    private LocalDateTime reportedAt;

    public PostMetric() {
    }

    public PostMetric(long id, long submissionId, int views, int likes, int comments, LocalDateTime reportedAt) {
        this.id = id;
        this.submissionId = submissionId;
        this.views = views;
        this.likes = likes;
        this.comments = comments;
        this.reportedAt = reportedAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(long submissionId) {
        this.submissionId = submissionId;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getComments() {
        return comments;
    }

    public void setComments(int comments) {
        this.comments = comments;
    }

    public LocalDateTime getReportedAt() {
        return reportedAt;
    }

    public void setReportedAt(LocalDateTime reportedAt) {
        this.reportedAt = reportedAt;
    }
}
