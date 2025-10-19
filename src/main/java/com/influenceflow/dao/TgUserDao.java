package com.influenceflow.dao;

import com.influenceflow.config.Db;
import com.influenceflow.model.TgUser;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

public class TgUserDao {
    private final DataSource dataSource;

    public TgUserDao() {
        this(Db.getDataSource());
    }

    public TgUserDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public TgUser save(TgUser user) {
        String sql = "INSERT INTO tg_user (username, is_admin, created_at) VALUES (?, ?, ?) RETURNING id";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setBoolean(2, user.isAdmin());
            ps.setTimestamp(3, Timestamp.valueOf(user.getCreatedAt()));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    user.setId(rs.getLong("id"));
                }
            }
            return user;
        } catch (SQLException e) {
            throw new DaoException("Failed to insert tg_user", e);
        }
    }

    public Optional<TgUser> findByUsername(String username) {
        String sql = "SELECT id, username, is_admin, created_at FROM tg_user WHERE username = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DaoException("Failed to query tg_user", e);
        }
    }

    public Optional<TgUser> findById(long id) {
        String sql = "SELECT id, username, is_admin, created_at FROM tg_user WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DaoException("Failed to query tg_user by id", e);
        }
    }

    private TgUser mapRow(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        String username = rs.getString("username");
        boolean isAdmin = rs.getBoolean("is_admin");
        LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
        return new TgUser(id, username, isAdmin, createdAt);
    }
}
