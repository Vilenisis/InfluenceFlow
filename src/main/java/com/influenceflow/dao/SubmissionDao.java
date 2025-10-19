package com.influenceflow.dao;

import com.influenceflow.config.Db;
import com.influenceflow.model.Submission;
import com.influenceflow.model.SubmissionStatus;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SubmissionDao {
    private final DataSource dataSource;

    public SubmissionDao() {
        this(Db.getDataSource());
    }

    public SubmissionDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Submission save(Submission submission) {
        String sql = "INSERT INTO submission (task_id, creator_id, url, status, submitted_at) "
                + "VALUES (?, ?, ?, ?, ?) RETURNING id";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, submission.getTaskId());
            ps.setLong(2, submission.getCreatorId());
            ps.setString(3, submission.getUrl());
            ps.setObject(4, submission.getStatus().name(), Types.OTHER);
            ps.setTimestamp(5, Timestamp.valueOf(submission.getSubmittedAt()));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    submission.setId(rs.getLong("id"));
                }
            }
            return submission;
        } catch (SQLException e) {
            throw new DaoException("Failed to insert submission", e);
        }
    }

    public List<Submission> findByStatus(SubmissionStatus status) {
        String sql = "SELECT id, task_id, creator_id, url, status, submitted_at, reviewed_at FROM submission WHERE status = ? "
                + "ORDER BY submitted_at";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, status.name(), Types.OTHER);
            try (ResultSet rs = ps.executeQuery()) {
                List<Submission> submissions = new ArrayList<>();
                while (rs.next()) {
                    submissions.add(mapRow(rs));
                }
                return submissions;
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to query submissions", e);
        }
    }

    public void updateStatus(long submissionId, SubmissionStatus status) {
        String sql = "UPDATE submission SET status = ?, reviewed_at = ? WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, status.name(), Types.OTHER);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(3, submissionId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to update submission status", e);
        }
    }

    public boolean existsByTaskAndCreator(long taskId, long creatorId) {
        String sql = "SELECT 1 FROM submission WHERE task_id = ? AND creator_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, taskId);
            ps.setLong(2, creatorId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to check submission uniqueness", e);
        }
    }

    private Submission mapRow(ResultSet rs) throws SQLException {
        Submission submission = new Submission();
        submission.setId(rs.getLong("id"));
        submission.setTaskId(rs.getLong("task_id"));
        submission.setCreatorId(rs.getLong("creator_id"));
        submission.setUrl(rs.getString("url"));
        String rawStatus = rs.getString("status");
        try {
            submission.setStatus(SubmissionStatus.fromDatabaseValue(rawStatus));
        } catch (IllegalArgumentException ex) {
            throw new DaoException("Unknown submission status value: " + rawStatus);
        }
        submission.setSubmittedAt(rs.getTimestamp("submitted_at").toLocalDateTime());
        Timestamp reviewedAt = rs.getTimestamp("reviewed_at");
        submission.setReviewedAt(reviewedAt != null ? reviewedAt.toLocalDateTime() : null);
        return submission;
    }
}
