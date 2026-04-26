package com.example.newsagent.feed;

import com.example.newsagent.dto.NewsItemDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class NewsPromptBuilder {

    private static final Logger log =
            LoggerFactory.getLogger(FeedCollectorService.class);

    public String build(List<NewsItemDto> articles) {

        // Pro Quelle maximal 5 Artikel — ausgewogene Verteilung
        var balancedArticles = articles.stream()
                .collect(Collectors.groupingBy(NewsItemDto::getSource))
                .values().stream()
                .flatMap(sourceArticles -> sourceArticles.stream().limit(5))
                .toList();

        log.info("Balanced: {} articles from {} sources",
                balancedArticles.size(),
                balancedArticles.stream()
                        .map(NewsItemDto::getSource)
                        .distinct()
                        .count());

        var sb = new StringBuilder();
        sb.append("""
            Du bist ein unabhängiger Nachrichtenanalyst mit journalistischer Erfahrung.
            
            Deine Aufgabe:
            Analysiere die folgenden Artikel und erstelle ein kompaktes Morgen-Briefing auf Deutsch.
            
            QUELLENVERGLEICH — das ist deine wichtigste Aufgabe:
            - Identifiziere Themen über die mehrere Quellen berichten
            - Vergleiche explizit wie verschiedene Quellen dasselbe Thema darstellen
            - Beispiel: "Der Standard betont X, während Die Presse eher Y hervorhebt"
            - Erkenne was eine Quelle betont und was sie möglicherweise weglässt
            - Erkenne politische, wirtschaftliche oder ideologische Perspektiven
            - Sei dabei neutral und objektiv
            
            LINKS:
            - Füge bei jedem erwähnten Artikel einen klickbaren Link ein
            - Format: <a href="URL">Artikeltitel</a>
            - Verwende NUR HTML Formatierung — KEIN Markdown
            
            STRUKTUR des Briefings:
            
            🔥 TOP THEMEN
            Für jedes Top-Thema (max 3):
            - Kurze Zusammenfassung (2-3 Sätze)
            - Quellenvergleich wenn mehrere Quellen berichten
            - Links zu den relevanten Artikeln
            
            🇦🇹 ÖSTERREICH
            - Wichtigste Entwicklungen aus Inland und Wirtschaft
            - Quellenvergleich zwischen Standard, ORF, Presse, Kurier
            - Links zu den Artikeln
            
            🌍 INTERNATIONAL
            - Wichtigste internationale Entwicklungen von BBC, DW, Guardian
            - Vergleich wie verschiedene Quellen berichten
            - Links zu den Artikeln
            
            💻 TECH & AI
            - Wichtigste Tech Neuigkeiten
            - Links zu den Artikeln
            
            📊 KONKLUSION
            - Kurzes Fazit des Tages (3-5 Sätze)
            - Wichtigste Trends und Entwicklungen
            - Auffällige Unterschiede in der Berichterstattung
            
            STIL:
            - Kompakt und prägnant — wird per Telegram gelesen
            - Keine langen Absätze
            - Direkt und auf den Punkt
            - Auf Deutsch
            - Verwende NUR HTML — KEIN Markdown
            
            Hier sind die heutigen Artikel:
            """);

        balancedArticles.forEach(article -> {
            sb.append("\n---\n");
            sb.append("Quelle: ").append(article.getSource()).append("\n");
            sb.append("Kategorie: ").append(article.getCategory()).append("\n");
            sb.append("Titel: ").append(article.getTitle()).append("\n");
            sb.append("URL: ").append(article.getUrl()).append("\n");
            if (article.getDescription() != null
                    && !article.getDescription().isBlank()) {
                sb.append("Beschreibung: ")
                        .append(article.getDescription()).append("\n");
            }
        });

        return sb.toString();
    }
}