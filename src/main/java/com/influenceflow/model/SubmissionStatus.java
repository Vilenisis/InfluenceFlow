package com.influenceflow.model;

public enum SubmissionStatus {
    PENDING,
    APPROVED,
    REJECTED;

    public static SubmissionStatus fromDatabaseValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Submission status value is null");
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Submission status value is blank");
        }
        try {
            return SubmissionStatus.valueOf(normalized.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unknown submission status: " + value, ex);
        }
    }
}
