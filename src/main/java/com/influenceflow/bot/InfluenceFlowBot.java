package com.influenceflow.bot;

import com.influenceflow.model.Campaign;
import com.influenceflow.model.Creator;
import com.influenceflow.model.Submission;
import com.influenceflow.model.SubmissionStatus;
import com.influenceflow.service.CampaignService;
import com.influenceflow.service.CreatorService;
import com.influenceflow.service.SubmissionService;
import com.influenceflow.util.RegexValidator;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class InfluenceFlowBot extends TelegramLongPollingBot {
    private final CreatorService creatorService;
    private final CampaignService campaignService;
    private final SubmissionService submissionService;

    public InfluenceFlowBot(CreatorService creatorService, CampaignService campaignService,
                            SubmissionService submissionService) {
        this.creatorService = creatorService;
        this.campaignService = campaignService;
        this.submissionService = submissionService;
    }

    @Override
    public String getBotUsername() {
        return Optional.ofNullable(System.getenv("BOT_USERNAME"))
                .orElseThrow(() -> new IllegalStateException("BOT_USERNAME is not set"));
    }

    @Override
    public String getBotToken() {
        return Optional.ofNullable(System.getenv("BOT_TOKEN"))
                .orElseThrow(() -> new IllegalStateException("BOT_TOKEN is not set"));
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }
        Message message = update.getMessage();
        String text = message.getText().trim();
        long chatId = message.getChatId();
        String username = message.getFrom() != null ? message.getFrom().getUserName() : null;

        try {
            if (text.startsWith("/start")) {
                handleStart(chatId, username, text);
            } else if (text.startsWith("/campaigns")) {
                handleCampaigns(chatId);
            } else if (text.startsWith("/submit")) {
                handleSubmit(chatId, username, text);
            } else if (text.startsWith("/mymetrics")) {
                handleMetrics(chatId, text);
            } else if (text.startsWith("/moderate")) {
                handleModerationList(chatId, username);
            } else if (text.startsWith("/approve")) {
                handleModerationAction(chatId, username, text, SubmissionStatus.APPROVED);
            } else if (text.startsWith("/reject")) {
                handleModerationAction(chatId, username, text, SubmissionStatus.REJECTED);
            } else {
                sendText(chatId, "Неизвестная команда. Доступные команды: /start, /campaigns, /submit, /mymetrics");
            }
        } catch (Exception e) {
            sendText(chatId, "Ошибка: " + e.getMessage());
        }
    }

    private void handleStart(long chatId, String username, String text) {
        String[] parts = text.replaceFirst("/start", "").trim().split(";");
        if (parts.length < 4) {
            sendText(chatId, "Формат команды: /start ФИО;email;ниша;@handle");
            return;
        }
        Creator creator = creatorService.registerCreator(username, parts[0].trim(), parts[1].trim(),
                parts[2].trim(), parts[3].trim());
        sendText(chatId, "Регистрация успешно завершена. Ваш идентификатор: " + creator.getId());
    }

    private void handleCampaigns(long chatId) {
        List<Campaign> campaigns = campaignService.getActiveCampaigns();
        if (campaigns.isEmpty()) {
            sendText(chatId, "Активных кампаний нет");
            return;
        }
        String response = campaigns.stream()
                .map(c -> "#" + c.getId() + " " + c.getName() + "\n" + c.getDescription())
                .collect(Collectors.joining("\n\n"));
        sendText(chatId, response);
    }

    private void handleSubmit(long chatId, String username, String text) {
        String[] parts = text.split(" ");
        if (parts.length < 3) {
            sendText(chatId, "Формат команды: /submit taskId URL");
            return;
        }
        long taskId = Long.parseLong(parts[1]);
        String url = parts[2];
        Creator creator = creatorService.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Сначала выполните /start"));
        Submission submission = submissionService.submitWork(taskId, creator.getId(), url);
        sendText(chatId, "Сдача отправлена с номером " + submission.getId());
    }

    private void handleMetrics(long chatId, String text) {
        String[] parts = text.split(" ", 3);
        if (parts.length < 3) {
            sendText(chatId, "Формат команды: /mymetrics submissionId views=123 likes=45 comments=6");
            return;
        }
        long submissionId = Long.parseLong(parts[1]);
        Map<String, Integer> metrics = RegexValidator.parseMetrics(parts[2]);
        submissionService.recordMetrics(submissionId, metrics);
        sendText(chatId, "Метрики сохранены для сдачи " + submissionId);
    }

    private void handleModerationList(long chatId, String username) {
        if (!isAdmin(username)) {
            sendText(chatId, "Недостаточно прав");
            return;
        }
        List<Submission> submissions = submissionService.getPendingSubmissions();
        if (submissions.isEmpty()) {
            sendText(chatId, "Нет сдач в статусе PENDING");
            return;
        }
        String body = submissions.stream()
                .map(s -> "ID:" + s.getId() + " task=" + s.getTaskId() + " creator=" + s.getCreatorId() + "\n" + s.getUrl())
                .collect(Collectors.joining("\n\n"));
        sendText(chatId, body + "\n\nДля утверждения: /approve ID, для отклонения: /reject ID");
    }

    private void handleModerationAction(long chatId, String username, String text, SubmissionStatus status) {
        if (!isAdmin(username)) {
            sendText(chatId, "Недостаточно прав");
            return;
        }
        String[] parts = text.split(" ");
        if (parts.length < 2) {
            sendText(chatId, "Укажите идентификатор сдачи");
            return;
        }
        long submissionId = Long.parseLong(parts[1]);
        submissionService.moderateSubmission(submissionId, status);
        sendText(chatId, "Статус обновлён: " + status);
    }

    private boolean isAdmin(String username) {
        String admin = System.getenv("BOT_ADMIN");
        return admin != null && username != null && admin.equalsIgnoreCase(username);
    }

    private void sendText(long chatId, String text) {
        SendMessage sendMessage = new SendMessage(Long.toString(chatId), text);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new IllegalStateException("Не удалось отправить сообщение", e);
        }
    }
}
