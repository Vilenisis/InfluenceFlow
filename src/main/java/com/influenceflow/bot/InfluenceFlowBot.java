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
        Long telegramUserId = message.getFrom() != null ? message.getFrom().getId() : null;
        String username = message.getFrom() != null ? message.getFrom().getUserName() : null;

        try {
            if (text.startsWith("/start")) {
                handleStart(chatId, telegramUserId, username, text);
            } else if (text.startsWith("/campaigns")) {
                handleCampaigns(chatId);
            } else if (text.startsWith("/submit")) {
                handleSubmit(chatId, telegramUserId, text);
            } else if (text.startsWith("/mymetrics")) {
                handleMetrics(chatId, text);
            } else if (text.startsWith("/moderate")) {
                handleModerationList(chatId, telegramUserId, username);
            } else if (text.startsWith("/approve")) {
                handleModerationAction(chatId, telegramUserId, username, text, SubmissionStatus.APPROVED);
            } else if (text.startsWith("/reject")) {
                handleModerationAction(chatId, telegramUserId, username, text, SubmissionStatus.REJECTED);
            } else {
                sendText(chatId, "Неизвестная команда. Доступные команды: /start, /campaigns, /submit, /mymetrics");
            }
        } catch (Exception e) {
            sendText(chatId, "Ошибка: " + e.getMessage());
        }
    }

    private void handleStart(long chatId, Long telegramUserId, String username, String text) {
        if (telegramUserId == null) {
            sendText(chatId, "Не удалось определить ваш Telegram ID. Попробуйте позже.");
            return;
        }
        String[] parts = text.replaceFirst("/start", "").trim().split(";");
        if (parts.length < 4) {
            sendText(chatId, "Формат команды: /start ФИО;email;ниша;@handle");
            return;
        }
        Creator creator = creatorService.registerCreator(telegramUserId, username, parts[0].trim(), parts[1].trim(),
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

    private void handleSubmit(long chatId, Long telegramUserId, String text) {
        if (telegramUserId == null) {
            sendText(chatId, "Не удалось определить ваш Telegram ID. Попробуйте позже.");
            return;
        }
        String[] parts = text.split(" ");
        if (parts.length < 3) {
            sendText(chatId, "Формат команды: /submit taskId URL");
            return;
        }
        long taskId = Long.parseLong(parts[1]);
        String url = parts[2];
        Creator creator = creatorService.findByTelegramId(telegramUserId)
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

    private void handleModerationList(long chatId, Long telegramUserId, String username) {
        if (!isAdmin(telegramUserId, username)) {
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

    private void handleModerationAction(long chatId, Long telegramUserId, String username, String text, SubmissionStatus status) {
        if (!isAdmin(telegramUserId, username)) {
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

    private boolean isAdmin(Long telegramUserId, String username) {
        if (telegramUserId != null && creatorService.isAdmin(telegramUserId)) {
            return true;
        }
        String adminEnv = System.getenv("BOT_ADMIN");
        if (adminEnv == null || adminEnv.isBlank()) {
            return false;
        }
        String[] tokens = adminEnv.split(",");
        for (String token : tokens) {
            String trimmed = token.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (telegramUserId != null) {
                try {
                    if (telegramUserId == Long.parseLong(trimmed)) {
                        return true;
                    }
                } catch (NumberFormatException ignored) {
                    // not a numeric id, fall back to username comparison
                }
            }
            if (username != null && trimmed.equalsIgnoreCase(username)) {
                return true;
            }
        }
        return false;
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
