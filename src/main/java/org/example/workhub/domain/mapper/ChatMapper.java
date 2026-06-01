package org.example.workhub.domain.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.workhub.domain.dto.response.ChatActionResponse;
import org.example.workhub.domain.dto.response.ChatConversationResponse;
import org.example.workhub.domain.dto.response.ChatHistoryMessageResponse;
import org.example.workhub.domain.dto.response.ChatSourceResponse;
import org.example.workhub.domain.entity.ChatConversation;
import org.example.workhub.domain.entity.ChatMessage;
import org.example.workhub.repository.ChatMessageRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ChatMapper {

    private final ObjectMapper objectMapper;
    private final ChatMessageRepository chatMessageRepository;

    public ChatConversationResponse toConversationResponse(ChatConversation conversation) {
        String preview = chatMessageRepository.findFirstByConversationIdOrderByCreatedDateDesc(conversation.getId())
                .map(ChatMessage::getContent)
                .map(this::preview)
                .orElse(null);
        return ChatConversationResponse.builder()
                .id(conversation.getId())
                .title(conversation.getTitle())
                .lastMessagePreview(preview)
                .lastMessageAt(conversation.getLastMessageAt())
                .createdAt(conversation.getCreatedDate())
                .build();
    }

    public ChatHistoryMessageResponse toHistoryResponse(ChatMessage message) {
        return ChatHistoryMessageResponse.builder()
                .id(message.getId())
                .senderType(message.getSenderType())
                .content(message.getContent())
                .intent(message.getIntent())
                .responseMode(message.getResponseMode())
                .outOfScope(message.getOutOfScope())
                .sources(readSources(message.getSourcesJson()))
                .suggestedActions(readActions(message.getActionsJson()))
                .createdAt(message.getCreatedDate())
                .build();
    }

    public String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value == null ? List.of() : value);
        } catch (Exception ex) {
            return "[]";
        }
    }

    private List<ChatSourceResponse> readSources(String json) {
        return readJson(json, new TypeReference<>() {});
    }

    private List<ChatActionResponse> readActions(String json) {
        return readJson(json, new TypeReference<>() {});
    }

    private <T> List<T> readJson(String json, TypeReference<List<T>> type) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception ex) {
            return List.of();
        }
    }

    private String preview(String content) {
        if (content == null || content.length() <= 160) {
            return content;
        }
        return content.substring(0, 157) + "...";
    }
}

