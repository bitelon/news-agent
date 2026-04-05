package com.example.newsagent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix="gemini")
@Data
public class GeminiConfig {
    private String apiKey;
    private String baseUrl;
    private String model;
    private int maxTokens;
}
