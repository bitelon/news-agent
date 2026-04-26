package com.example.newsagent.api;

import com.example.newsagent.dto.NewsItemDto;
import com.example.newsagent.feed.ClaudeAnalysisService;
import com.example.newsagent.feed.FeedCollectorService;
import com.example.newsagent.feed.NewsAnalysisService;
import com.example.newsagent.feed.TelegramService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/news")
public class NewsController {

    private static final Logger log =
            LoggerFactory.getLogger(NewsController.class);

    private final FeedCollectorService collectorService;
    private final NewsAnalysisService newsAnalysisService;
    private final TelegramService telegramService;

    public NewsController(FeedCollectorService collectorService,
                          NewsAnalysisService newsAnalysisService,
                          TelegramService telegramService) {
        this.collectorService = collectorService;
        this.newsAnalysisService = newsAnalysisService;
        this.telegramService = telegramService;
    }

    @GetMapping
    public ResponseEntity<List<NewsItemDto>> getAllNews() {
        log.info("GET /api/news called");
        var news = collectorService.collectAll();
        return ResponseEntity.ok(news);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<NewsItemDto>> getByCategory(
            @PathVariable String category) {
        var news = collectorService.collectAll().stream()
                .filter(item -> category.equalsIgnoreCase(item.getCategory()))
                .toList();
        return ResponseEntity.ok(news);
    }
    @GetMapping("/briefing")
    public ResponseEntity<String> getBriefing() {
        log.info("GET /api/news/briefing called");
        var articles = collectorService.collectAll();
        var briefing = newsAnalysisService.analyze(articles);
        log.info("Briefing length: {} chars", briefing.length());
        log.info("Briefing content: {}", briefing);
        this.telegramService.sendBriefingToUser(briefing);
        return ResponseEntity.ok(briefing);
    }
}