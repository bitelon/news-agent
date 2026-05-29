package com.example.newsagent;

import com.example.newsagent.config.NewsSourcesConfig;
import com.example.newsagent.dto.NewsItemDto;
import com.example.newsagent.exception.FeedFetchException;
import com.example.newsagent.feed.FeedCollectorService;
import com.example.newsagent.feed.RssFeedClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class FeedCollectorServiceTest {
    @Mock
    private RssFeedClient rssFeedClient;
    @Mock
    private NewsSourcesConfig config;
    @InjectMocks
    private FeedCollectorService feedCollectorService;

    @Test
    void shouldCollectArticlesFromAllSources() {
        var feedConfig = new NewsSourcesConfig.RssFeedConfig();
        feedConfig.setUrl("https://derstandard.att/rss/inland");
        feedConfig.setCategory("inland");

        when(config.getRss()).thenReturn(List.of(feedConfig));
        when(rssFeedClient.fetchFeed(anyString(), anyString(), anyString()))
                .thenReturn(List.of(
                        new NewsItemDto("Titel 1", "Beschreibung 1",
                                "https://url1.com", "derstandard.at",
                                LocalDateTime.now(), "inland")
                ));

        var result = feedCollectorService.collectAll();
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getSource()).isEqualTo("derstandard.at");
    }

    @Test
    void shouldSkipFeedAndContinueWhenOneFails() {
        var feedSuccess = new NewsSourcesConfig.RssFeedConfig();
        feedSuccess.setUrl("https://derstandard.att/rss/inland");
        feedSuccess.setCategory("inland");

        var feedFail = new NewsSourcesConfig.RssFeedConfig();
        feedFail.setUrl("https://diePresse.at/rss/inland");
        feedFail.setCategory("inland");

        when(config.getRss()).thenReturn(List.of(feedSuccess, feedFail));


        when(rssFeedClient.fetchFeed(eq("https://derstandard.att/rss/inland"), anyString(), anyString()))
                .thenReturn(List.of(
                        new NewsItemDto("Titel 1", "Beschreibung 1",
                                "https://url1.com", "derstandard.at",
                                LocalDateTime.now(), "inland")
                ));

        when(rssFeedClient.fetchFeed(eq("https://diePresse.at/rss/inland"), anyString(), anyString()))
                .thenThrow(new FeedFetchException("https://diepresse.at/rss", "Feed not available", null));

        var result = feedCollectorService.collectAll();
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getSource()).isEqualTo("derstandard.at");
    }

    @Test
    void shouldReturnEmptyListWhenAllFeedsFail() {
        var feedFail = new NewsSourcesConfig.RssFeedConfig();
        feedFail.setUrl("https://diePresse.at/rss/inland");
        feedFail.setCategory("inland");

        when(config.getRss()).thenReturn(List.of(feedFail));
        when(rssFeedClient.fetchFeed(anyString(), anyString(), anyString()))
                .thenThrow(new FeedFetchException("https://diepresse.at/rss", "Feed not available", null));


        var result = feedCollectorService.collectAll();
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenNoSourcesConfigured() {
        when(config.getRss()).thenReturn(List.of());
        var result = feedCollectorService.collectAll();
        assertThat(result).isEmpty();
    }

}
