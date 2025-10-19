package com.influenceflow.dao;

import com.influenceflow.config.Db;
import com.influenceflow.model.Payout;
import com.influenceflow.model.PayoutItem;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class PayoutDao {
    private final DataSource dataSource;

    public PayoutDao() {
        this(Db.getDataSource());
    }

    public PayoutDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Payout save(Payout payout) {
        String sql = "INSERT INTO payout (creator_id, total_amount, created_at, status) VALUES (?, ?, ?, ?) RETURNING id";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, payout.getCreatorId());
            ps.setBigDecimal(2, payout.getTotalAmount());
            ps.setTimestamp(3, Timestamp.valueOf(payout.getCreatedAt()));
            ps.setString(4, payout.getStatus());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    payout.setId(rs.getLong("id"));
                }
            }
            return payout;
        } catch (SQLException e) {
            throw new DaoException("Failed to insert payout", e);
        }
    }

    public void saveItems(long payoutId, List<PayoutItem> items) {
        String sql = "INSERT INTO payout_item (payout_id, submission_id, amount) VALUES (?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            for (PayoutItem item : items) {
                ps.setLong(1, payoutId);
                ps.setLong(2, item.getSubmissionId());
                ps.setBigDecimal(3, item.getAmount());
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new DaoException("Failed to insert payout items", e);
        }
    }

    public List<Payout> findByStatus(String status) {
        String sql = "SELECT id, creator_id, total_amount, created_at, status FROM payout WHERE status = ? ORDER BY created_at";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                List<Payout> payouts = new ArrayList<>();
                while (rs.next()) {
                    payouts.add(mapRow(rs));
                }
                return payouts;
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to query payouts", e);
        }
    }

    private Payout mapRow(ResultSet rs) throws SQLException {
        Payout payout = new Payout();
        payout.setId(rs.getLong("id"));
        payout.setCreatorId(rs.getLong("creator_id"));
        payout.setTotalAmount(rs.getBigDecimal("total_amount"));
        payout.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        payout.setStatus(rs.getString("status"));
        return payout;
    }
}
