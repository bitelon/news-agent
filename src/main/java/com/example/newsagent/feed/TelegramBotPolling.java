package com.example.newsagent.feed;

import com.example.newsagent.config.TelegramConfig;
import com.example.newsagent.dto.TelegramMessageDto;
import com.example.newsagent.dto.TelegramUpdatesDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;


@Component
public class TelegramBotPolling {

    private static final Logger log =
            LoggerFactory.getLogger(TelegramBotPolling.class);

    private final TelegramConfig config;
    private final ChatIdRepository chatIdRepository;
    private final WebClient webClient;
    private final FeedCollectorService collectorService;
    private final NewsAnalysisService newsAnalysisService;
    private int lastUpdateId = 0;

    public TelegramBotPolling(
            TelegramConfig config,
            ChatIdRepository chatIdRepository, FeedCollectorService collectorService, NewsAnalysisService newsAnalysisService) {
        this.config = config;
        this.chatIdRepository = chatIdRepository;
        this.webClient = WebClient.builder()
                .baseUrl(config.getBaseUrl())
                .defaultHeader("content-type", "application/json")
                .build();
        this.collectorService = collectorService;
        this.newsAnalysisService = newsAnalysisService;
    }

    @Scheduled(fixedDelay = 3000) // alle 3 Sekunden prüfen
    public void pollUpdates() {
        try {
            var response = webClient.get()
                    .uri("/bot" + config.getBotToken() +
                            "/getUpdates?offset=" + (lastUpdateId + 1) +
                            "&timeout=1")
                    .retrieve()
                    .bodyToMono(TelegramUpdatesDto.class)
                    .block();

            if (response == null || response.getResult() == null) return;

            response.getResult().forEach(update -> {
                lastUpdateId = update.getUpdateId();
                handleUpdate(update);
            });

        } catch (Exception e) {
            log.error("Polling error: {}", e.getMessage());
        }
    }

    private void handleUpdate(TelegramUpdatesDto.Update update) {
        if (update.getMessage() == null) return;

        var chatId = String.valueOf(update.getMessage().getChat().getId());
        var text = update.getMessage().getText();

        if (text == null) return;

        switch (text.trim()) {
            case "/start" -> {
                chatIdRepository.add(chatId);
                sendMessage(chatId,
                        "✅ Du hast den News Agent abonniert!\n" +
                                "Du bekommst täglich um 08:00 Uhr dein Morgen-Briefing.\n\n" +
                                "⏳ Dein erstes Briefing wird gerade erstellt...");

                // Sofortiges Briefing in eigenem Thread
                // damit der Polling-Loop nicht blockiert wird
                new Thread(() -> {
                    try {
                        var articles = collectorService.collectAll();
                        var briefing = newsAnalysisService.analyze(articles);
                        sendMessage(chatId, briefing);
                    } catch (Exception e) {
                        log.error("Could not send welcome briefing", e);
                        sendMessage(chatId, "❌ Briefing konnte nicht erstellt werden.");
                    }
                }).start();

                log.info("New subscriber: {}", chatId);
            }
            case "/stop" -> {
                chatIdRepository.remove(chatId);
                sendMessage(chatId,
                        "❌ Du hast den News Agent abgemeldet.\n" +
                                "Schreib /start um dich wieder anzumelden.");
                log.info("Unsubscribed: {}", chatId);
            }
            default -> sendMessage(chatId,
                    "Verfügbare Befehle:\n" +
                            "/start → Morgen-Briefing abonnieren\n" +
                            "/stop  → Abonnement beenden");
        }
    }

    private void sendMessage(String chatId, String text) {
        try {
            var request = TelegramMessageDto.builder()
                    .chatId(chatId)
                    .text(text)
                    .parseMode("HTML")
                    .build();

            webClient.post()
                    .uri("/bot" + config.getBotToken() + "/sendMessage")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            log.error("Could not send message to {}: {}", chatId, e.getMessage());
        }
    }
}
