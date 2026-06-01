package org.example.workhub.domain.dto.internal;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatAiHistoryItem {
    private String senderType;
    private String content;
}

