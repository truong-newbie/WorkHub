package org.example.workhub.domain.dto.internal;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatAiResponseRequest {
    private String message;
    private String intent;
    private List<ChatAiHistoryItem> recentMessages;
    private List<Map<String, Object>> contextItems;
}

