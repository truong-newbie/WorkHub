package org.example.workhub.domain.dto.response;

import lombok.*;
import org.example.workhub.constant.ChatIntent;
import org.example.workhub.constant.ChatResponseMode;
import org.example.workhub.constant.ChatSenderType;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatHistoryMessageResponse {
    private Long id;
    private ChatSenderType senderType;
    private String content;
    private ChatIntent intent;
    private ChatResponseMode responseMode;
    private Boolean outOfScope;
    private List<ChatSourceResponse> sources;
    private List<ChatActionResponse> suggestedActions;
    private LocalDateTime createdAt;
}

