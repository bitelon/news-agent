package com.example.newsagent.feed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NewsScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(NewsScheduler.class);

    private final FeedCollectorService collectorService;
    private final NewsAnalysisService analysisService;
    private final TelegramService telegramService;

    public NewsScheduler(
            FeedCollectorService collectorService,
            NewsAnalysisService analysisService,
            TelegramService telegramService) {
        this.collectorService = collectorService;
        this.analysisService = analysisService;
        this.telegramService = telegramService;
    }

    @Scheduled(cron = "0 0 7 * * *") // jeden Tag um 7 in der früh
    //@Scheduled(cron = "0 * * * * *")  // jede Minute
    public void sendMorningBriefing() {
        log.info("Scheduler triggered — starting morning briefing");
        try {
            var articles = collectorService.collectAll();
            var briefing = analysisService.analyze(articles);
            telegramService.sendBriefingToUser(briefing);
            log.info("Morning briefing sent successfully");
        } catch (Exception e) {
            log.error("Morning briefing failed", e);
        }
    }
}
