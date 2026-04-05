package com.example.newsagent.feed;

import com.example.newsagent.dto.NewsItemDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NewsPromptBuilder {

    public String build(List<NewsItemDto> articles) {
        var sb = new StringBuilder();
        sb.append("""
            Du bist ein Nachrichtenanalyst. 
            Analysiere die folgenden Artikel und erstelle ein kompaktes 
            Morgen-Briefing auf Deutsch.
                        
            Struktur:
            1. Top 3 wichtigste Themen des Tages (je 2-3 Sätze)
            2. Österreich aktuell (inland + wirtschaft)
            3. International
            4. Tech & AI
                        
            Zeige bei kontroversen Themen verschiedene Perspektiven.
            Halte es kurz und prägnant — es wird per Telegram verschickt.
                        
            Hier sind die heutigen Artikel:
            """);

        articles.stream()
            .limit(30)
            .forEach(article -> {
                sb.append("\n---\n");
                sb.append("Quelle: ").append(article.getSource()).append("\n");
                sb.append("Kategorie: ").append(article.getCategory()).append("\n");
                sb.append("Titel: ").append(article.getTitle()).append("\n");
                if (article.getDescription() != null
                        && !article.getDescription().isBlank()) {
                    sb.append("Beschreibung: ")
                      .append(article.getDescription()).append("\n");
                }
            });

        return sb.toString();
    }
}