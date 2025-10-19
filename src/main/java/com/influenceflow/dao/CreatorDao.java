package com.influenceflow.dao;

import com.influenceflow.config.Db;
import com.influenceflow.model.Creator;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class CreatorDao {
    private final DataSource dataSource;

    public CreatorDao() {
        this(Db.getDataSource());
    }

    public CreatorDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Creator save(Creator creator) {
        String sql = "INSERT INTO creator (tg_user_id, full_name, email, niche, platform_handle) VALUES (?, ?, ?, ?, ?) RETURNING id";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, creator.getTgUserId());
            ps.setString(2, creator.getFullName());
            ps.setString(3, creator.getEmail());
            ps.setString(4, creator.getNiche());
            ps.setString(5, creator.getPlatformHandle());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    creator.setId(rs.getLong("id"));
                }
            }
            return creator;
        } catch (SQLException e) {
            throw new DaoException("Failed to insert creator", e);
        }
    }

    public Creator update(Creator creator) {
        String sql = "UPDATE creator SET full_name = ?, email = ?, niche = ?, platform_handle = ? WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, creator.getFullName());
            ps.setString(2, creator.getEmail());
            ps.setString(3, creator.getNiche());
            ps.setString(4, creator.getPlatformHandle());
            ps.setLong(5, creator.getId());
            ps.executeUpdate();
            return creator;
        } catch (SQLException e) {
            throw new DaoException("Failed to update creator", e);
        }
    }

    public Optional<Creator> findByTgUserId(long tgUserId) {
        String sql = "SELECT id, tg_user_id, full_name, email, niche, platform_handle FROM creator WHERE tg_user_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, tgUserId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DaoException("Failed to query creator", e);
        }
    }

    public Optional<Creator> findById(long id) {
        String sql = "SELECT id, tg_user_id, full_name, email, niche, platform_handle FROM creator WHERE id = ?";
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
            throw new DaoException("Failed to query creator by id", e);
        }
    }

    public boolean existsById(long id) {
        String sql = "SELECT 1 FROM creator WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to check creator existence", e);
        }
    }

    private Creator mapRow(ResultSet rs) throws SQLException {
        Creator creator = new Creator();
        creator.setId(rs.getLong("id"));
        creator.setTgUserId(rs.getLong("tg_user_id"));
        creator.setFullName(rs.getString("full_name"));
        creator.setEmail(rs.getString("email"));
        creator.setNiche(rs.getString("niche"));
        creator.setPlatformHandle(rs.getString("platform_handle"));
        return creator;
    }
}
