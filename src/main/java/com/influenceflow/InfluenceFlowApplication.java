package com.influenceflow;

import com.influenceflow.bot.InfluenceFlowBot;
import com.influenceflow.dao.CampaignDao;
import com.influenceflow.dao.CreatorDao;
import com.influenceflow.dao.PostMetricDao;
import com.influenceflow.dao.SubmissionDao;
import com.influenceflow.dao.TaskDao;
import com.influenceflow.dao.TgUserDao;
import com.influenceflow.service.CampaignService;
import com.influenceflow.service.CreatorService;
import com.influenceflow.service.SubmissionService;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public final class InfluenceFlowApplication {
    private InfluenceFlowApplication() {
    }

    public static void main(String[] args) throws TelegramApiException {
        TgUserDao tgUserDao = new TgUserDao();
        CreatorDao creatorDao = new CreatorDao();
        CampaignDao campaignDao = new CampaignDao();
        SubmissionDao submissionDao = new SubmissionDao();
        PostMetricDao postMetricDao = new PostMetricDao();
        TaskDao taskDao = new TaskDao();

        CreatorService creatorService = new CreatorService(tgUserDao, creatorDao);
        CampaignService campaignService = new CampaignService(campaignDao, taskDao);
        SubmissionService submissionService = new SubmissionService(submissionDao, postMetricDao);

        InfluenceFlowBot bot = new InfluenceFlowBot(creatorService, campaignService, submissionService);
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(bot);
    }
}
