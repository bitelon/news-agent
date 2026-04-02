package com.example.newsagent.exception;

public class FeedFetchException extends RuntimeException {

    private final String feedUrl;

    public FeedFetchException(String feedUrl, String message, Throwable cause) {
        super(message, cause);
        this.feedUrl = feedUrl;
    }

    public String getFeedUrl() {
        return feedUrl;
    }
}
