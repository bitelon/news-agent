package com.example.newsagent.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaudeRequestDto {
    private String model;

    @JsonProperty("max_tokens")
    private int maxTokens;

    private List<ClaudeMessageDto> messages;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClaudeMessageDto {
        private String role;
        private String content;
    }
}