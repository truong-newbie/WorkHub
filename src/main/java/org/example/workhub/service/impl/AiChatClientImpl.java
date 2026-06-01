package org.example.workhub.service.impl;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.example.workhub.constant.ErrorMessage;
import org.example.workhub.domain.dto.internal.*;
import org.example.workhub.exception.InternalServerException;
import org.example.workhub.service.AiChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Component
@Slf4j
public class AiChatClientImpl implements AiChatClient {

    @Value("${ai.worker.base-url:http://localhost:8000}")
    private String baseUrl;

    @Value("${chat.ai.connect-timeout-seconds:5}")
    private long connectTimeoutSeconds;

    @Value("${chat.ai.read-timeout-seconds:30}")
    private long readTimeoutSeconds;

    private RestTemplate restTemplate;

    @PostConstruct
    void init() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(connectTimeoutSeconds));
        requestFactory.setReadTimeout(Duration.ofSeconds(readTimeoutSeconds));
        restTemplate = new RestTemplate(requestFactory);
    }

    @Override
    public ChatAiIntentResponse classifyIntent(ChatAiIntentRequest request) {
        return post("/api/v1/ai/chat/intent", request, ChatAiIntentResponse.class);
    }

    @Override
    public ChatAiResponse generateResponse(ChatAiResponseRequest request) {
        return post("/api/v1/ai/chat/respond", request, ChatAiResponse.class);
    }

    private <T> T post(String path, Object request, Class<T> responseType) {
        try {
            ResponseEntity<T> response = restTemplate.postForEntity(baseUrl + path, request, responseType);
            if (response.getBody() == null) {
                throw new InternalServerException(ErrorMessage.Chat.ERR_AI_UNAVAILABLE);
            }
            return response.getBody();
        } catch (RestClientException ex) {
            log.warn("AI chat worker request failed path={} cause={}", path, ex.getClass().getSimpleName());
            throw new InternalServerException(ErrorMessage.Chat.ERR_AI_UNAVAILABLE);
        }
    }
}

