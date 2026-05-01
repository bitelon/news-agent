package com.example.newsagent.feed;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
public class ChatIdRepository {

    private static final Logger log =
            LoggerFactory.getLogger(ChatIdRepository.class);

    private final String filePath;
    private final ObjectMapper objectMapper;

    public ChatIdRepository(
            @Value("${telegram.chat-ids-file:/root/chat-ids.json}")
            String filePath,
            ObjectMapper objectMapper) {
        this.filePath = filePath;
        this.objectMapper = objectMapper;
        initFile();
    }

    private void initFile() {
        var file = new File(filePath);
        if (!file.exists()) {
            try {
                objectMapper.writeValue(file, new ArrayList<String>());
                log.info("Created chat IDs file: {}", filePath);
            } catch (Exception e) {
                log.error("Could not create chat IDs file", e);
            }
        }
    }

    public List<String> findAll() {
        try {
            return objectMapper.readValue(
                    new File(filePath),
                    new TypeReference<List<String>>() {}
            );
        } catch (Exception e) {
            log.error("Could not read chat IDs", e);
            return new ArrayList<>();
        }
    }

    public void add(String chatId) {
        var ids = new ArrayList<>(findAll());
        if (!ids.contains(chatId)) {
            ids.add(chatId);
            save(ids);
            log.info("Added chat ID: {}", chatId);
        }
    }

    public void remove(String chatId) {
        var ids = new ArrayList<>(findAll());
        ids.remove(chatId);
        save(ids);
        log.info("Removed chat ID: {}", chatId);
    }

    private void save(List<String> ids) {
        try {
            objectMapper.writeValue(new File(filePath), ids);
        } catch (Exception e) {
            log.error("Could not save chat IDs", e);
        }
    }
}