package com.influenceflow.service;

import com.influenceflow.dao.CampaignDao;
import com.influenceflow.dao.TaskDao;
import com.influenceflow.model.Campaign;
import com.influenceflow.model.Task;

import java.util.List;

public class CampaignService {
    private final CampaignDao campaignDao;
    private final TaskDao taskDao;

    public CampaignService(CampaignDao campaignDao, TaskDao taskDao) {
        this.campaignDao = campaignDao;
        this.taskDao = taskDao;
    }

    public List<Campaign> getActiveCampaigns() {
        return campaignDao.findActiveCampaigns();
    }

    public List<Task> getTasksForCampaign(long campaignId) {
        return taskDao.findByCampaign(campaignId);
    }

    public List<Task> getActiveTasks() {
        return taskDao.findActiveTasks();
    }
}
