package com.example.newsagent.config;

import com.example.newsagent.feed.ClaudeAnalysisService;
import com.example.newsagent.feed.GeminiAnalysisService;
import com.example.newsagent.feed.NewsAnalysisService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AnalysisConfig {

    @Value("${news.analysis.provider}")
    private String provider;

    @Bean
    @Primary
    public NewsAnalysisService analysisService(
            ClaudeAnalysisService claude,
            GeminiAnalysisService gemini) {
        return switch (provider) {
            case "claude" -> claude;
            case "gemini" -> gemini;
            default -> throw new IllegalArgumentException(
                "Unknown provider: " + provider + 
                ". Valid values: claude, gemini"
            );
        };
    }
}