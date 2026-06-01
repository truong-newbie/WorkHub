package org.example.workhub.service;

import org.example.workhub.domain.dto.pagination.PaginationResponseDto;
import org.example.workhub.domain.dto.request.ChatMessageRequest;
import org.example.workhub.domain.dto.response.ChatConversationResponse;
import org.example.workhub.domain.dto.response.ChatHistoryMessageResponse;
import org.example.workhub.domain.dto.response.ChatMessageResponse;

public interface ChatService {
    ChatMessageResponse sendMessage(ChatMessageRequest request);

    PaginationResponseDto<ChatConversationResponse> getMyConversations(int page, int size);

    PaginationResponseDto<ChatHistoryMessageResponse> getMessages(Long conversationId, int page, int size);

    void deleteConversation(Long conversationId);
}

