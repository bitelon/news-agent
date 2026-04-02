package com.example.newsagent.feed;

import java.net.URI;
import java.util.List;

import com.example.newsagent.config.NewsSourcesConfig;
import com.example.newsagent.dto.NewsItemDto;
import com.example.newsagent.exception.FeedFetchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FeedCollectorService {

    private static final Logger log =
            LoggerFactory.getLogger(FeedCollectorService.class);

    private final RssFeedClient rssFeedClient;
    private final NewsSourcesConfig config;

    public FeedCollectorService(
            RssFeedClient rssFeedClient,
            NewsSourcesConfig config) {
        this.rssFeedClient = rssFeedClient;
        this.config = config;
    }

    public List<NewsItemDto> collectAll() {
        log.info("Starting news collection from all sources");

        var allNews = config.getRss().stream()
                .flatMap(feedConfig -> fetchSafely(feedConfig).stream())
                .toList();

        log.info("Collected {} articles total", allNews.size());
        return allNews;
    }

    private List<NewsItemDto> fetchSafely(
            NewsSourcesConfig.RssFeedConfig feedConfig) {
        try {
            return rssFeedClient.fetchFeed(
                    feedConfig.getUrl(),
                    extractSourceName(feedConfig.getUrl()),
                    feedConfig.getCategory()
            );
        } catch (FeedFetchException e) {
            // Ein Feed schlägt fehl — aber die anderen laufen weiter
            log.warn("Skipping feed {} due to error: {}",
                    feedConfig.getUrl(), e.getMessage());
            return List.of();
        }
    }

    private String extractSourceName(String url) {
        try {
            return URI.create(url).getHost()
                    .replace("www.", "")
                    .replace("rss.", "");
        } catch (Exception e) {
            return url;
        }
    }
}