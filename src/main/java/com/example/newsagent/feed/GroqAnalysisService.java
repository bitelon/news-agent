package com.example.newsagent.feed;

import com.example.newsagent.config.GroqConfig;
import com.example.newsagent.dto.GroqRequestDto;
import com.example.newsagent.dto.GroqResponseDto;
import com.example.newsagent.dto.NewsItemDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service("groq")
public class GroqAnalysisService implements NewsAnalysisService {

    private static final Logger log =
            LoggerFactory.getLogger(GroqAnalysisService.class);

    private final WebClient webClient;
    private final GroqConfig config;
    private final NewsPromptBuilder newsPromptBuilder;

    public GroqAnalysisService(GroqConfig config, NewsPromptBuilder newsPromptBuilder) {
        this.config = config;
        this.newsPromptBuilder = newsPromptBuilder;
        this.webClient = WebClient.builder()
                .baseUrl(config.getBaseUrl())
                .defaultHeader("content-type", "application/json")
                .defaultHeader("Authorization", "Bearer " +this.config.getApiKey())
                .build();
    }


    @Override
    public String analyze(List<NewsItemDto> articles) {
        log.info("Sending {} articles to Groq for analysis", articles.size());

        var prompt = newsPromptBuilder.build(articles);

        var request = GroqRequestDto.builder()
                .model(config.getModel())
                .messages(List.of(
                        GroqRequestDto.GroqMessageDto.builder()
                                .role("user")
                                .content(prompt)
                                .build()
                ))
                .build();

        try {
            var response = webClient.post()
                    .uri("/openai/v1/chat/completions")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(GroqResponseDto.class)
                    .block();

            if (response == null) {
                throw new RuntimeException("Empty response from Groq");
            }

            log.info("Groq analysis completed successfully");
            return response.getFirstText();

        } catch (Exception e) {
            log.error("Failed to get analysis from Groq", e);
            throw new RuntimeException("Groq analysis failed", e);
        }
    }
}
