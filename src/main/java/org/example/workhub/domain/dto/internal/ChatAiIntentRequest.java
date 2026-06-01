package org.example.workhub.domain.dto.internal;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatAiIntentRequest {
    private String message;
    private List<ChatAiHistoryItem> recentMessages;
}

