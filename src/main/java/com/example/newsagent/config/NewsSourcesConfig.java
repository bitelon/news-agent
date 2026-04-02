package com.example.newsagent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "news.sources")
@Data
public class NewsSourcesConfig {

    private List<RssFeedConfig> rss;
    private NewsApiConfig newsapi;

    @Data
    public static class RssFeedConfig {
        private String url;
        private String category;
    }

    @Data
    public static class NewsApiConfig {
        private String baseUrl;
        private String apiKey;
        private String country;
    }
}
