package com.example.newsagent.feed;

import com.example.newsagent.dto.NewsApiArticleDto;
import com.example.newsagent.dto.NewsItemDto;
import com.rometools.rome.feed.synd.SyndEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class NewsItemMapper {

    private static final Logger log =
            LoggerFactory.getLogger(NewsItemMapper.class);

    public NewsItemDto fromNewsApiArticle(
            NewsApiArticleDto article,
            String category) {

        return NewsItemDto.builder()
                .title(article.getTitle())
                .description(article.getDescription())
                .url(article.getUrl())
                .source(article.getSource() != null
                        ? article.getSource().getName()
                        : "unknown")
                .publishedAt(parseDate(article.getPublishedAt()))
                .category(category)
                .build();
    }

    public NewsItemDto fromRssEntry(SyndEntry entry, String source, String category) {
        return NewsItemDto.builder()
                .title(entry.getTitle())
                .description(entry.getDescription() != null
                        ? entry.getDescription().getValue()
                        : "")
                .url(entry.getLink())
                .source(source)
                .publishedAt(entry.getPublishedDate() != null
                        ? entry.getPublishedDate().toInstant()
                          .atZone(ZoneId.systemDefault())
                          .toLocalDateTime()
                        : LocalDateTime.now())
                .category(category)
                .build();
    }

    private LocalDateTime parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return LocalDateTime.now();
        }
        try {
            return ZonedDateTime
                    .parse(dateStr, DateTimeFormatter.ISO_DATE_TIME)
                    .withZoneSameInstant(ZoneId.of("Europe/Vienna"))
                    .toLocalDateTime();
        } catch (Exception e) {
            log.warn("Could not parse date: {}", dateStr);
            return LocalDateTime.now();
        }
    }
}
