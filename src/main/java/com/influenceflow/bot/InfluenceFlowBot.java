package com.influenceflow.bot;

import com.influenceflow.dao.DaoException;
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

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InfluenceFlowBot extends TelegramLongPollingBot {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final String HELP_MESSAGE = String.join("\n",
            "Доступные команды:",
            "/start ФИО;email;ниша;@handle — регистрация креатора",
            "/campaigns — список активных кампаний",
            "/submit taskId URL — отправить работу на модерацию",
            "/mymetrics submissionId views=123 likes=45 comments=6 — зафиксировать метрики",
            "/moderate — очередь сдач (для администраторов)",
            "/approve submissionId — утвердить сдачу",
            "/reject submissionId причина — отклонить сдачу",
            "/payouts — ближайшие выплаты (для администраторов)",
            "/reports — сводка по активности");

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

        if (text.isEmpty()) {
            sendHelp(chatId);
            return;
        }

        String command = text.startsWith("/") ? text.split("\\s+", 2)[0].toLowerCase() : "";

        try {
            switch (command) {
                case "/start":
                    handleStart(chatId, telegramUserId, username, text);
                    break;
                case "/campaigns":
                    handleCampaigns(chatId);
                    break;
                case "/submit":
                    handleSubmit(chatId, telegramUserId, text);
                    break;
                case "/mymetrics":
                    handleMetrics(chatId, text);
                    break;
                case "/moderate":
                    handleModerationList(chatId, telegramUserId, username);
                    break;
                case "/approve":
                    handleModerationAction(chatId, telegramUserId, username, text, SubmissionStatus.APPROVED);
                    break;
                case "/reject":
                    handleModerationAction(chatId, telegramUserId, username, text, SubmissionStatus.REJECTED);
                    break;
                case "/help":
                    sendHelp(chatId);
                    break;
                case "/payouts":
                    handlePayouts(chatId, telegramUserId, username);
                    break;
                case "/reports":
                    handleReports(chatId, telegramUserId, username);
                    break;
                default:
                    sendError(chatId, "Неизвестная команда. Наберите /help, чтобы увидеть список возможностей.");
                    break;
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            sendError(chatId, e.getMessage());
        } catch (DaoException e) {
            logError("Database error", e);
            sendError(chatId, "Сервис временно недоступен. Попробуйте повторить запрос чуть позже.");
        } catch (Exception e) {
            logError("Unexpected error", e);
            sendError(chatId, "Произошла непредвиденная ошибка. Мы уже работаем над её устранением.");
        }
    }

    private void handleStart(long chatId, Long telegramUserId, String username, String text) {
        if (telegramUserId == null) {
            sendText(chatId, "Не удалось определить ваш Telegram ID. Попробуйте позже.");
            return;
        }
        String payload = text.length() > 6 ? text.substring(6).trim() : "";
        if (payload.isEmpty()) {
            sendText(chatId, "Формат команды: /start ФИО;email;ниша;@handle");
            return;
        }
        String[] parts = payload.split(";");
        if (parts.length < 4) {
            sendText(chatId, "Недостаточно данных. Формат: /start ФИО;email;ниша;@handle");
            return;
        }
        Creator creator = creatorService.registerCreator(telegramUserId, username, parts[0].trim(), parts[1].trim(),
                parts[2].trim(), parts[3].trim());
        sendText(chatId, "Регистрация успешно завершена. Ваш идентификатор: " + creator.getId()
                + "\nТеперь вы можете посмотреть кампании командой /campaigns");
    }

    private void handleCampaigns(long chatId) {
        List<Campaign> campaigns = campaignService.getActiveCampaigns();
        if (campaigns.isEmpty()) {
            sendText(chatId, "Активных кампаний нет");
            return;
        }
        StringBuilder response = new StringBuilder("📢 Активные кампании:\n\n");
        for (Campaign campaign : campaigns) {
            response.append(formatCampaign(campaign)).append("\n\n");
        }
        response.append("Выберите задачу и сдайте работу командой /submit taskId URL");
        sendText(chatId, response.toString().trim());
    }

    private void handleSubmit(long chatId, Long telegramUserId, String text) {
        if (telegramUserId == null) {
            sendText(chatId, "Не удалось определить ваш Telegram ID. Попробуйте позже.");
            return;
        }
        String[] parts = text.trim().split("\\s+", 3);
        if (parts.length < 3) {
            sendText(chatId, "Формат команды: /submit taskId URL");
            return;
        }
        long taskId = parseLong(parts[1], "taskId");
        String url = parts[2];
        Creator creator = creatorService.findByTelegramId(telegramUserId)
                .orElseThrow(() -> new IllegalStateException("Сначала выполните /start"));
        Submission submission = submissionService.submitWork(taskId, creator.getId(), url);
        sendText(chatId, "Готово! Сдача №" + submission.getId() + " отправлена на модерацию.");
    }

    private void handleMetrics(long chatId, String text) {
        String[] parts = text.trim().split("\\s+", 3);
        if (parts.length < 3) {
            sendText(chatId, "Формат команды: /mymetrics submissionId views=123 likes=45 comments=6");
            return;
        }
        long submissionId = parseLong(parts[1], "submissionId");
        Map<String, Integer> metrics = RegexValidator.parseMetrics(parts[2]);
        submissionService.recordMetrics(submissionId, metrics);
        sendText(chatId, "Метрики сохранены для сдачи №" + submissionId);
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
        StringBuilder body = new StringBuilder("🛡 Очередь модерации:\n\n");
        for (Submission submission : submissions) {
            body.append("• ID ").append(submission.getId())
                    .append(" | task=").append(submission.getTaskId())
                    .append(" | creator=").append(submission.getCreatorId())
                    .append("\n")
                    .append(submission.getUrl()).append("\n\n");
        }
        body.append("Для утверждения: /approve submissionId\nДля отклонения: /reject submissionId причина");
        sendText(chatId, body.toString().trim());
    }

    private void handleModerationAction(long chatId, Long telegramUserId, String username, String text, SubmissionStatus status) {
        if (!isAdmin(telegramUserId, username)) {
            sendText(chatId, "Недостаточно прав");
            return;
        }
        String[] parts = text.trim().split("\\s+", 3);
        if (parts.length < 2) {
            sendText(chatId, "Укажите идентификатор сдачи: /" + status.name().toLowerCase() + " submissionId");
            return;
        }
        long submissionId = parseLong(parts[1], "submissionId");
        submissionService.moderateSubmission(submissionId, status);
        StringBuilder reply = new StringBuilder("Статус сдачи №").append(submissionId)
                .append(" обновлён: ").append(status);
        if (status == SubmissionStatus.REJECTED && parts.length == 3 && !parts[2].trim().isEmpty()) {
            reply.append("\nПричина: ").append(parts[2].trim());
        }
        sendText(chatId, reply.toString());
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

    private void handlePayouts(long chatId, Long telegramUserId, String username) {
        if (!isAdmin(telegramUserId, username)) {
            sendText(chatId, "Недостаточно прав для просмотра выплат");
            return;
        }
        sendText(chatId, "Раздел выплат появится в ближайшем обновлении. Следите за новостями!");
    }

    private void handleReports(long chatId, Long telegramUserId, String username) {
        if (!isAdmin(telegramUserId, username)) {
            sendText(chatId, "Недостаточно прав для просмотра отчётов");
            return;
        }
        sendText(chatId, "Раздел аналитики ещё готовится. Используйте /moderate для работы со сдачами.");
    }

    private long parseLong(String value, String fieldName) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Некорректное значение для " + fieldName + ": " + value);
        }
    }

    private void sendHelp(long chatId) {
        sendText(chatId, HELP_MESSAGE);
    }

    private String formatCampaign(Campaign campaign) {
        StringBuilder builder = new StringBuilder();
        builder.append("#").append(campaign.getId()).append(" ").append(campaign.getName()).append("\n")
                .append(campaign.getDescription());
        if (campaign.getStartDate() != null && campaign.getEndDate() != null) {
            builder.append("\nПериод: ")
                    .append(DATE_FORMAT.format(campaign.getStartDate()))
                    .append(" — ")
                    .append(DATE_FORMAT.format(campaign.getEndDate()));
        }
        if (campaign.getRewardPerSubmission() != null) {
            builder.append("\nВознаграждение за сдачу: ").append(campaign.getRewardPerSubmission());
        }
        return builder.toString();
    }

    private void sendError(long chatId, String message) {
        sendText(chatId, "⚠️ " + message);
    }

    private void sendText(long chatId, String text) {
        SendMessage sendMessage = new SendMessage(Long.toString(chatId), text);
        sendMessage.disableWebPagePreview();
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new IllegalStateException("Не удалось отправить сообщение", e);
        }
    }

    private void logError(String context, Exception e) {
        System.err.println(context + ": " + e.getMessage());
    }
}
