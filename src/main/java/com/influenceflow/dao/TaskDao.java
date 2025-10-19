package com.influenceflow.dao;

import com.influenceflow.config.Db;
import com.influenceflow.model.Task;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TaskDao {
    private final DataSource dataSource;

    public TaskDao() {
        this(Db.getDataSource());
    }

    public TaskDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Task> findActiveTasks() {
        String sql = "SELECT id, campaign_id, title, brief, platform, payout_amount, deadline FROM task "
                + "WHERE deadline >= CURRENT_DATE ORDER BY deadline";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Task> tasks = new ArrayList<>();
            while (rs.next()) {
                tasks.add(mapRow(rs));
            }
            return tasks;
        } catch (SQLException e) {
            throw new DaoException("Failed to query tasks", e);
        }
    }

    public List<Task> findByCampaign(long campaignId) {
        String sql = "SELECT id, campaign_id, title, brief, platform, payout_amount, deadline FROM task "
                + "WHERE campaign_id = ? ORDER BY deadline";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, campaignId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Task> tasks = new ArrayList<>();
                while (rs.next()) {
                    tasks.add(mapRow(rs));
                }
                return tasks;
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to query tasks by campaign", e);
        }
    }

    public Task save(Task task) {
        String sql = "INSERT INTO task (campaign_id, title, brief, platform, payout_amount, deadline) "
                + "VALUES (?, ?, ?, ?, ?, ?) RETURNING id";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, task.getCampaignId());
            ps.setString(2, task.getTitle());
            ps.setString(3, task.getBrief());
            ps.setString(4, task.getPlatform());
            ps.setBigDecimal(5, task.getPayoutAmount());
            ps.setDate(6, Date.valueOf(task.getDeadline()));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    task.setId(rs.getLong("id"));
                }
            }
            return task;
        } catch (SQLException e) {
            throw new DaoException("Failed to insert task", e);
        }
    }

    private Task mapRow(ResultSet rs) throws SQLException {
        Task task = new Task();
        task.setId(rs.getLong("id"));
        task.setCampaignId(rs.getLong("campaign_id"));
        task.setTitle(rs.getString("title"));
        task.setBrief(rs.getString("brief"));
        task.setPlatform(rs.getString("platform"));
        task.setPayoutAmount(rs.getBigDecimal("payout_amount"));
        Date deadline = rs.getDate("deadline");
        task.setDeadline(deadline != null ? deadline.toLocalDate() : null);
        return task;
    }
}
