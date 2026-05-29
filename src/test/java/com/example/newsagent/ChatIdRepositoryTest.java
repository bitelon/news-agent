package com.example.newsagent;

import com.example.newsagent.feed.ChatIdRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class ChatIdRepositoryTest {


    private ChatIdRepository chatIdRepository;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        var tempFile = tempDir.resolve("chat-ids.json").toString();
        chatIdRepository = new ChatIdRepository(tempFile, new ObjectMapper());
    }

    @Test
    void shouldAddChatIdToList() {
        var id = "telegram_id_1";
        chatIdRepository.add(id);

        List<String> idList = chatIdRepository.findAll();

        assertThat(idList).isNotEmpty();
        assertThat(idList).hasSize(1);
        assertThat(idList.getFirst()).isEqualTo("telegram_id_1");

    }

    @Test
    void shouldRemoveChatIdFromList() {
        chatIdRepository.add("telegram_id_1");
        chatIdRepository.add("telegram_id_2");

        chatIdRepository.remove("telegram_id_1");

        List<String> idList = chatIdRepository.findAll();

        assertThat(idList).isNotEmpty();
        assertThat(idList).hasSize(1);
        assertThat(idList.getFirst()).isEqualTo("telegram_id_2");


    }

    @Test
    void shouldNotAddDuplicateChatId() {

        var id = "telegram_id_1";
        chatIdRepository.add(id);
        chatIdRepository.add(id);

        List<String> idList = chatIdRepository.findAll();

        assertThat(idList).isNotEmpty();
        assertThat(idList).hasSize(1);
        assertThat(idList.getFirst()).isEqualTo("telegram_id_1");
    }

    @Test
    void shouldReturnEmptyListOnInit() {
        List<String> idList = chatIdRepository.findAll();
        assertThat(idList).isEmpty();
    }

    @Test
    void shouldNotFailWhenRemovingNonExistentId() {
        chatIdRepository.remove("telegram_id_not_exist");

        assertThat(chatIdRepository.findAll()).isEmpty();

    }

}
