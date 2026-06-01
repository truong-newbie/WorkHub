package org.example.workhub.domain.dto.response;

import lombok.*;
import org.example.workhub.constant.ChatActionType;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatActionResponse {
    private ChatActionType type;
    private String label;
    private String url;
}

