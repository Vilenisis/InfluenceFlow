package com.influenceflow.dao;

import com.influenceflow.config.Db;
import com.influenceflow.model.PostMetric;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

public class PostMetricDao {
    private final DataSource dataSource;

    public PostMetricDao() {
        this(Db.getDataSource());
    }

    public PostMetricDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public PostMetric save(PostMetric metric) {
        String sql = "INSERT INTO post_metric (submission_id, views, likes, comments, reported_at) "
                + "VALUES (?, ?, ?, ?, ?) RETURNING id";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, metric.getSubmissionId());
            ps.setInt(2, metric.getViews());
            ps.setInt(3, metric.getLikes());
            ps.setInt(4, metric.getComments());
            ps.setTimestamp(5, Timestamp.valueOf(metric.getReportedAt()));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    metric.setId(rs.getLong("id"));
                }
            }
            return metric;
        } catch (SQLException e) {
            throw new DaoException("Failed to insert post_metric", e);
        }
    }

    public Optional<PostMetric> findLatestBySubmission(long submissionId) {
        String sql = "SELECT id, submission_id, views, likes, comments, reported_at FROM post_metric "
                + "WHERE submission_id = ? ORDER BY reported_at DESC LIMIT 1";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, submissionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DaoException("Failed to query post_metric", e);
        }
    }

    private PostMetric mapRow(ResultSet rs) throws SQLException {
        PostMetric metric = new PostMetric();
        metric.setId(rs.getLong("id"));
        metric.setSubmissionId(rs.getLong("submission_id"));
        metric.setViews(rs.getInt("views"));
        metric.setLikes(rs.getInt("likes"));
        metric.setComments(rs.getInt("comments"));
        Timestamp timestamp = rs.getTimestamp("reported_at");
        metric.setReportedAt(timestamp != null ? timestamp.toLocalDateTime() : LocalDateTime.now());
        return metric;
    }
}
