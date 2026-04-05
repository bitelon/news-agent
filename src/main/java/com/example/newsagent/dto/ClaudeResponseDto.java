package com.example.newsagent.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClaudeResponseDto {
    private String id;
    private List<ContentBlock> content;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContentBlock {
        private String type;
        private String text;
    }

    public String getFirstText() {
        if (content == null || content.isEmpty()) {
            return "";
        }
        return content.stream()
            .filter(block -> "text".equals(block.getType()))
            .map(ContentBlock::getText)
            .findFirst()
            .orElse("");
    }
}