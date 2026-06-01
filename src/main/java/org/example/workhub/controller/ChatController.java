package org.example.workhub.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.workhub.base.RestApiV1;
import org.example.workhub.base.VsResponseUtil;
import org.example.workhub.constant.UrlConstant;
import org.example.workhub.domain.dto.request.ChatMessageRequest;
import org.example.workhub.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestApiV1
@RequiredArgsConstructor
@PreAuthorize("hasRole('CANDIDATE')")
public class ChatController {

    private final ChatService chatService;

    @PostMapping(UrlConstant.Chat.MESSAGES)
    public ResponseEntity<?> sendMessage(@RequestBody @Valid ChatMessageRequest request) {
        return VsResponseUtil.success(chatService.sendMessage(request));
    }

    @GetMapping(UrlConstant.Chat.CONVERSATIONS)
    public ResponseEntity<?> getMyConversations(@RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "20") int size) {
        return VsResponseUtil.success(chatService.getMyConversations(page, size));
    }

    @GetMapping(UrlConstant.Chat.CONVERSATION_MESSAGES)
    public ResponseEntity<?> getMessages(@PathVariable Long conversationId,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "30") int size) {
        return VsResponseUtil.success(chatService.getMessages(conversationId, page, size));
    }

    @DeleteMapping(UrlConstant.Chat.CONVERSATION_ID)
    public ResponseEntity<?> deleteConversation(@PathVariable Long conversationId) {
        chatService.deleteConversation(conversationId);
        return VsResponseUtil.success("Conversation deleted successfully");
    }
}

