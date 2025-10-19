package com.influenceflow.dao;

import com.influenceflow.config.Db;
import com.influenceflow.model.Campaign;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CampaignDao {
    private final DataSource dataSource;

    public CampaignDao() {
        this(Db.getDataSource());
    }

    public CampaignDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Campaign> findActiveCampaigns() {
        String sql = "SELECT id, name, description, start_date, end_date, reward_per_submission "
                + "FROM campaign WHERE end_date >= CURRENT_DATE ORDER BY start_date";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Campaign> campaigns = new ArrayList<>();
            while (rs.next()) {
                campaigns.add(mapRow(rs));
            }
            return campaigns;
        } catch (SQLException e) {
            throw new DaoException("Failed to query campaigns", e);
        }
    }

    public Campaign save(Campaign campaign) {
        String sql = "INSERT INTO campaign (name, description, start_date, end_date, reward_per_submission) "
                + "VALUES (?, ?, ?, ?, ?) RETURNING id";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, campaign.getName());
            ps.setString(2, campaign.getDescription());
            ps.setDate(3, Date.valueOf(campaign.getStartDate()));
            ps.setDate(4, Date.valueOf(campaign.getEndDate()));
            ps.setBigDecimal(5, campaign.getRewardPerSubmission());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    campaign.setId(rs.getLong("id"));
                }
            }
            return campaign;
        } catch (SQLException e) {
            throw new DaoException("Failed to insert campaign", e);
        }
    }

    private Campaign mapRow(ResultSet rs) throws SQLException {
        Campaign campaign = new Campaign();
        campaign.setId(rs.getLong("id"));
        campaign.setName(rs.getString("name"));
        campaign.setDescription(rs.getString("description"));
        campaign.setStartDate(rs.getDate("start_date").toLocalDate());
        campaign.setEndDate(rs.getDate("end_date").toLocalDate());
        campaign.setRewardPerSubmission(rs.getBigDecimal("reward_per_submission"));
        return campaign;
    }
}
