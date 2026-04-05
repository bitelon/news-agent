package com.example.newsagent.feed;

import com.example.newsagent.dto.NewsItemDto;

import java.util.List;

public interface NewsAnalysisService {
    String analyze(List<NewsItemDto> articles);
}
