package com.example.newsagent.feed;

import com.example.newsagent.config.AnthropicConfig;
import com.example.newsagent.config.GeminiConfig;
import com.example.newsagent.dto.GeminiRequestDto;
import com.example.newsagent.dto.GeminiResponseDto;
import com.example.newsagent.dto.NewsItemDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service("gemini")
public class GeminiAnalysisService implements NewsAnalysisService {

    private static final Logger log =
            LoggerFactory.getLogger(GeminiAnalysisService.class);

    private final WebClient webClient;
    private final GeminiConfig config;
    private final NewsPromptBuilder promptBuilder;

    public GeminiAnalysisService(
            GeminiConfig config,
            NewsPromptBuilder promptBuilder) {
        this.config = config;
        this.promptBuilder = promptBuilder;
        this.webClient = WebClient.builder()
                .baseUrl(config.getBaseUrl())
                .defaultHeader("content-type", "application/json")
                .build();
    }

    @Override
    public String analyze(List<NewsItemDto> articles) {
        log.info("Sending {} articles to Gemini for analysis", articles.size());

        var prompt = promptBuilder.build(articles);

        var request = GeminiRequestDto.builder()
                .contents(List.of(
                        GeminiRequestDto.Content.builder()
                                .parts(List.of(
                                        GeminiRequestDto.Part.builder()
                                                .text(prompt)
                                                .build()
                                ))
                                .build()
                ))
                .generationConfig(
                        GeminiRequestDto.GenerationConfig.builder()
                                .maxOutputTokens(config.getMaxTokens())
                                .build()
                )
                .build();

        try {
            var response = webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1beta/models/{model}:generateContent")
                            .queryParam("key", config.getApiKey())
                            .build(config.getModel())
                    )
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(GeminiResponseDto.class)
                    .block();

            if (response == null) {
                throw new RuntimeException("Empty response from Gemini");
            }

            log.info("Gemini analysis completed successfully");
            return response.getFirstText();

        } catch (Exception e) {
            log.error("Failed to get analysis from Gemini", e);
            throw new RuntimeException("Gemini analysis failed", e);
        }
    }
}