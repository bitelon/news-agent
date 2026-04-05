package com.example.newsagent.exception;

import lombok.Getter;

@Getter
public class FeedFetchException extends RuntimeException {

    private final String feedUrl;

    public FeedFetchException(String feedUrl, String message, Throwable cause) {
        super(message, cause);
        this.feedUrl = feedUrl;
    }

}
