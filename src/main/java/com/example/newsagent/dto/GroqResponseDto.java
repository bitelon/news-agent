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
public class GroqResponseDto {

    List<Choices> choices;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Choices {
        Messages message;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Messages {
        String role;
        String content;
    }

    public String getFirstText() {
        if (choices == null || choices.isEmpty()) {
            return "";
        }
        return choices.stream()
                .findFirst()
                .map(Choices::getMessage)
                .map(Messages::getContent)
                .orElse("");
    }
}
