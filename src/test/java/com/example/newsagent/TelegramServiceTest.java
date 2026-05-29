package com.example.newsagent;

import com.example.newsagent.config.TelegramConfig;
import com.example.newsagent.feed.ChatIdRepository;
import com.example.newsagent.feed.TelegramService;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static io.netty.handler.codec.http.HttpHeaders.addHeader;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

@ExtendWith(MockitoExtension.class)
public class TelegramServiceTest {

    static MockWebServer mockWebServer;
    public TelegramService telegramService;
    private TelegramConfig telegramConfig;
    @Mock
    ChatIdRepository chatIdRepository;


    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @BeforeEach
    void initialize() throws IOException {
        setUp();
        telegramConfig = new TelegramConfig();
        telegramConfig.setBaseUrl(mockWebServer.url("/").toString());
        telegramConfig.setBotToken("test-token");
        telegramService = new TelegramService(telegramConfig, chatIdRepository);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();;
    }


    @Test
    void shouldSendBriefingToAllUsers() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"ok\": true}")
                .addHeader("Content-Type", "application/json"));

        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"ok\": true}")
                .addHeader("Content-Type", "application/json"));


        List<String> chatIdList = new ArrayList<>();
        chatIdList.add("chatId-1");
        chatIdList.add("chatId-2");
        Mockito.when(chatIdRepository.findAll()).thenReturn(chatIdList);


        telegramService.sendBriefingToUser("Test Briefing Message");

        var request1 = mockWebServer.takeRequest();
        var body1 = request1.getBody().readUtf8();
        assertThat(request1.getPath()).contains("sendMessage");
        assertThat(body1).contains("Test Briefing Message");
        assertThat(body1).contains("chatId-1");

        var request2 = mockWebServer.takeRequest();
        var body2 = request2.getBody().readUtf8();
        assertThat(request2.getPath()).contains("sendMessage");
        assertThat(body2).contains("Test Briefing Message");
        assertThat(body2).contains("chatId-2");
    }

    @Test
    void shouldSendBriefingWithHtmlCorrectly() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"ok\": true}")
                .addHeader("Content-Type", "application/json"));


        List<String> chatIdList = new ArrayList<>();
        chatIdList.add("chatId-1");
        Mockito.when(chatIdRepository.findAll()).thenReturn(chatIdList);


        telegramService.sendBriefingToUser("<strong>Test Briefing Message</strong>");

        var request1 = mockWebServer.takeRequest();
        var body1 = request1.getBody().readUtf8();
        assertThat(request1.getPath()).contains("sendMessage");
        assertThat(body1).contains("<strong>Test Briefing Message</strong>");
        assertThat(body1).contains("HTML");


    }

    @Test
    void shouldNotFailWithoutChatIds() throws InterruptedException {
        Mockito.when(chatIdRepository.findAll()).thenReturn(List.of());
        assertThatCode(() -> telegramService.sendBriefingToUser("Test Briefing Message")).doesNotThrowAnyException();
    }

    @Test
    void shouldNotFailWhenOneClientFails() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500)
                .setBody("{}")
                .addHeader("Content-Type", "application/json"));

        mockWebServer.enqueue(new MockResponse().setResponseCode(200)
                .setBody("{\"ok\": true}")
                .addHeader("Content-Type", "application/json"));


        List<String> chatIdList = new ArrayList<>();
        chatIdList.add("chatId-1");
        chatIdList.add("chatId-2");
        Mockito.when(chatIdRepository.findAll()).thenReturn(chatIdList);


        assertThatCode(() -> telegramService.sendBriefingToUser("Test Briefing Message"))
                .doesNotThrowAnyException();

        var request1 = mockWebServer.takeRequest();

        var request2 = mockWebServer.takeRequest();
        var body2 = request2.getBody().readUtf8();
        assertThat(request2.getPath()).contains("sendMessage");
        assertThat(body2).contains("Test Briefing Message");
        assertThat(body2).contains("chatId-2");
    }

}
