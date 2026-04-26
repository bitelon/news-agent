package com.example.newsagent.feed;

import com.example.newsagent.config.TelegramConfig;
import com.example.newsagent.dto.TelegramMessageDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service("telegram")
public class TelegramService {

    private static final Logger log =
            LoggerFactory.getLogger(TelegramService.class);

    private final WebClient webClient;
    private final TelegramConfig config;

    public TelegramService(TelegramConfig config) {
        this.config = config;
        this.webClient = WebClient.builder()
                .baseUrl(config.getBaseUrl())
                .defaultHeader("content-type", "application/json")
                .build();
    }

    public void sendBriefingToUser(String text) {
        var request = TelegramMessageDto.builder()
                .chatId(config.getChatId())
                .text(text)
                .parseMode("HTML")
                .build();
        try {
            var response = webClient.post()
                    .uri("/bot" + config.getBotToken() + "/sendMessage")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response == null) {
                throw new RuntimeException("Message could not be sent");
            }

            log.info("Telegram Message sent successfully");

        } catch (Exception e) {
            log.error("Faild to send message via Telegram Bot", e);
            throw new RuntimeException("Faild to send message via Telegram Bot", e);
        }
    }
}
