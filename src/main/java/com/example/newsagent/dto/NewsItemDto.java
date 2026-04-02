package com.example.newsagent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsItemDto {
    private String title;
    private String description;
    private String url;
    private String source;
    private LocalDateTime publishedAt;
    private String category;
}