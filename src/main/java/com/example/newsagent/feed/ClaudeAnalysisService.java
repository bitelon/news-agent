package com.example.newsagent.feed;

import com.example.newsagent.config.AnthropicConfig;
import com.example.newsagent.dto.ClaudeRequestDto;
import com.example.newsagent.dto.ClaudeResponseDto;
import com.example.newsagent.dto.NewsItemDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service("claude")
public class ClaudeAnalysisService implements NewsAnalysisService{

    private static final Logger log =
            LoggerFactory.getLogger(ClaudeAnalysisService.class);

    private final WebClient webClient;
    private final AnthropicConfig config;
    private final NewsPromptBuilder newsPromptBuilder;

    public ClaudeAnalysisService(AnthropicConfig config, NewsPromptBuilder newsPromptBuilder) {
        this.config = config;
        this.newsPromptBuilder = newsPromptBuilder;
        this.webClient = WebClient.builder()
                .baseUrl(config.getBaseUrl())
                .defaultHeader("x-api-key", config.getApiKey())
                .defaultHeader("anthropic-version", "2023-06-01")
                .defaultHeader("content-type", "application/json")
                .build();
    }
    @Override
    public String analyze(List<NewsItemDto> articles) {
        log.info("Sending {} articles to Claude for analysis", articles.size());

        var prompt = newsPromptBuilder.build(articles);

        var request = ClaudeRequestDto.builder()
                .model(config.getModel())
                .maxTokens(config.getMaxTokens())
                .messages(List.of(
                        ClaudeRequestDto.ClaudeMessageDto.builder()
                                .role("user")
                                .content(prompt)
                                .build()
                ))
                .build();

        try {
            var response = webClient.post()
                    .uri("/v1/messages")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(ClaudeResponseDto.class)
                    .block();

            if (response == null) {
                throw new RuntimeException("Empty response from Claude");
            }

            log.info("Claude analysis completed successfully");
            return response.getFirstText();

        } catch (Exception e) {
            log.error("Failed to get analysis from Claude", e);
            throw new RuntimeException("Claude analysis failed", e);
        }
    }

}