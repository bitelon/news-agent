package com.example.newsagent.feed;

import com.example.newsagent.dto.NewsItemDto;
import com.example.newsagent.exception.FeedFetchException;
import com.rometools.rome.io.SyndFeedInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.sql.rowset.spi.XmlReader;
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
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");

            var input = new SyndFeedInput();
            var feed = input.build(
                    new InputStreamReader(
                            connection.getInputStream(),
                            StandardCharsets.UTF_8
                    )
            );

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
