package com.example.newsagent;

import com.example.newsagent.dto.NewsItemDto;
import com.example.newsagent.feed.NewsItemMapper;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.time.LocalDateTime;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NewsItemMapperTest {

    NewsItemMapper newsItemMapper = new NewsItemMapper();
    @Mock
    SyndEntry syndEntry;
    @Mock
    SyndContent syndContent;



    @Test
    void shouldBuildNewsItemDtoWithValidInputs() {
        when(syndEntry.getTitle()).thenReturn("TestTitle");
        when(syndEntry.getLink()).thenReturn("https://testurl.com");
        when(syndEntry.getPublishedDate()).thenReturn(new Date(1748764800000L));
        when(syndContent.getValue()).thenReturn("testValue");
        when(syndEntry.getDescription()).thenReturn(syndContent);

        NewsItemDto newsItemDto = newsItemMapper.fromRssEntry(syndEntry, "testSource", "testCategory");

        assertThat(newsItemDto.getTitle()).isEqualTo("TestTitle");
        assertThat(newsItemDto.getSource()).isEqualTo("testSource");
        assertThat(newsItemDto.getDescription()).isEqualTo("testValue");
        assertThat(newsItemDto.getUrl()).isEqualTo("https://testurl.com");

    }

    @Test
    void shouldBuildNewsItmDtoWhenDescriptionNull() {
        when(syndEntry.getTitle()).thenReturn("TestTitle");
        when(syndEntry.getLink()).thenReturn("https://testurl.com");
        when(syndEntry.getPublishedDate()).thenReturn(new Date(1748764800000L));
        when(syndEntry.getDescription()).thenReturn(null);

        NewsItemDto newsItemDto = newsItemMapper.fromRssEntry(syndEntry, "testSource", "testCategory");

        assertThat(newsItemDto.getTitle()).isEqualTo("TestTitle");
        assertThat(newsItemDto.getSource()).isEqualTo("testSource");
        assertThat(newsItemDto.getDescription()).isEqualTo("");
        assertThat(newsItemDto.getUrl()).isEqualTo("https://testurl.com");
    }

    @Test
    void shouldMapPublishedDateCorrectly() {
        var date = new Date(1748764800000L); // fixer Timestamp
        when(syndEntry.getTitle()).thenReturn("Titel");
        when(syndEntry.getLink()).thenReturn("https://url.com");
        when(syndEntry.getPublishedDate()).thenReturn(date);
        when(syndEntry.getDescription()).thenReturn(null);

        var result = newsItemMapper.fromRssEntry(syndEntry, "source", "category");

        assertThat(result.getPublishedAt()).isNotNull();
    }

    @Test
    void shouldUseCurrentTimeWhenPublishedDateNull() {
        when(syndEntry.getTitle()).thenReturn("Titel");
        when(syndEntry.getLink()).thenReturn("https://url.com");
        when(syndEntry.getPublishedDate()).thenReturn(null);
        when(syndEntry.getDescription()).thenReturn(null);

        var before = LocalDateTime.now();
        var result = newsItemMapper.fromRssEntry(syndEntry, "source", "category");
        var after = LocalDateTime.now();

        assertThat(result.getPublishedAt())
                .isAfterOrEqualTo(before)
                .isBeforeOrEqualTo(after);
    }
}
