package org.example.workhub.domain.dto.response;

import lombok.*;
import org.example.workhub.constant.ChatIntent;
import org.example.workhub.constant.ChatResponseMode;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {
    private Long conversationId;
    private String answer;
    private ChatIntent intent;
    private Boolean outOfScope;
    private ChatResponseMode responseMode;
    private List<ChatSourceResponse> sources;
    private List<ChatActionResponse> suggestedActions;
    private LocalDateTime createdAt;
}

