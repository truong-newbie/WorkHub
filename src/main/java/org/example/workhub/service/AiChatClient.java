package org.example.workhub.service;

import org.example.workhub.domain.dto.internal.*;

public interface AiChatClient {
    ChatAiIntentResponse classifyIntent(ChatAiIntentRequest request);

    ChatAiResponse generateResponse(ChatAiResponseRequest request);
}

