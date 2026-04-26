package com.example.newsagent.feed;

import com.example.newsagent.dto.NewsItemDto;
import com.example.newsagent.exception.FeedFetchException;
import com.rometools.rome.io.SyndFeedInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.sql.rowset.spi.XmlReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class RssFeedClient {

    private static final Logger log =
            LoggerFactory.getLogger(RssFeedClient.class);

    private final NewsItemMapper mapper;

    public RssFeedClient(NewsItemMapper mapper) {
        this.mapper = mapper;
    }

    public List<NewsItemDto> fetchFeed(String url, String source, String category) {
        log.debug("Fetching RSS feed from: {}", url);
        try {
            var connection = URI.create(url).toURL().openConnection();
            connection.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (X11; Linux x86_64; rv:109.0) Gecko/20100101 Firefox/115.0");
            connection.setRequestProperty("Accept",
                    "application/rss+xml, application/xml, text/xml, */*");
            connection.setRequestProperty("Accept-Language",
                    "de-AT,de;q=0.9,en;q=0.8");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            // BOM entfernen
            var inputStream = connection.getInputStream();
            var reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            var buffered = new BufferedReader(reader);

            // Erste Zeile lesen und BOM entfernen falls vorhanden
            buffered.mark(4);
            int firstChar = buffered.read();
            if (firstChar != '\uFEFF') {
                // Kein BOM — zurück zum Anfang
                buffered.reset();
            }
            // Falls BOM → einfach weiterlesen, BOM ist bereits konsumiert

            var input = new SyndFeedInput();
            var feed = input.build(buffered);

            log.debug("Fetched {} entries from {}",
                    feed.getEntries().size(), source);

            return feed.getEntries().stream()
                    .map(entry -> mapper.fromRssEntry(entry, source, category))
                    .toList();

        } catch (Exception e) {
            throw new FeedFetchException(url,
                    "Failed to fetch RSS feed from: " + url, e);
        }
    }
}
