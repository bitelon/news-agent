package com.example.newsagent;

import com.example.newsagent.dto.NewsItemDto;
import com.example.newsagent.feed.NewsPromptBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class NewsPromptBuilderTest {

    @InjectMocks
    private NewsPromptBuilder newsPromptBuilder;

    @Test
    void buildPromptWithArticles() {
        var articles = List.of(
                new NewsItemDto("Titel 1 ", "Beschreibung 1", "https://url1.com", "derStandard.at", LocalDateTime.now(), "inland"),
                new NewsItemDto("Titel 2 ", "Beschreibung 2", "https://url2.com", "orf.at", LocalDateTime.now(), "inland")
        );

        String prompt = newsPromptBuilder.build(articles);
        assertThat(prompt).isNotNull();
        assertThat(prompt).isNotEmpty();
        assertThat(prompt).contains("Titel 1");
        assertThat(prompt).contains("derStandard.at");
        assertThat(prompt).contains("orf.at");
    }

    @Test
    void shouldLimitArticlesPerSource() {
        // Arrange — 10 Artikel von derselben Quelle
        var articles = IntStream.range(0, 10)
                .mapToObj(i -> new NewsItemDto(
                        "Titel " + i, "Beschreibung " + i,
                        "https://url" + i + ".com", "derstandard.at",
                        LocalDateTime.now(), "inland"))
                .toList();

        // Act
        var prompt = newsPromptBuilder.build(articles);

        // Assert — maximal 5 pro Quelle
        var count = prompt.split("derstandard.at").length - 1;
        assertThat(count).isLessThanOrEqualTo(5);
    }

    @Test
    void shouldHandleEmptyArticleList() {
        // Act
        var prompt = newsPromptBuilder.build(List.of());

        // Assert — kein Crash, Prompt trotzdem valide
        assertThat(prompt).isNotNull();
        assertThat(prompt).isNotEmpty();
    }

    @Test
    void promptShouldContainHtmlInstruction() {
        var articles = List.of(
                new NewsItemDto("Titel", "Beschreibung", "https://url.com",
                        "derstandard.at", LocalDateTime.now(), "inland"));

        var prompt = newsPromptBuilder.build(articles);

        assertThat(prompt).contains("HTML");
        assertThat(prompt).contains("<a href=");


    }

    @Test
    void promptShouldContainDefinedStructure() {
        var articles = List.of(
                new NewsItemDto("Titel", "Beschreibung", "https://url.com",
                        "derstandard.at", LocalDateTime.now(), "inland"));

        var prompt = newsPromptBuilder.build(articles);

        assertThat(prompt).contains("TOP THEMEN");
        assertThat(prompt).contains("ÖSTERREICH");
        assertThat(prompt).contains("INTERNATIONAL");
        assertThat(prompt).contains("TECH & AI");
        assertThat(prompt).contains("KONKLUSION");
    }

}
