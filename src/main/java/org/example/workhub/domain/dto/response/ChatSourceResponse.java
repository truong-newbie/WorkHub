package org.example.workhub.domain.dto.response;

import lombok.*;
import org.example.workhub.constant.ChatSourceType;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSourceResponse {
    private ChatSourceType type;
    private String id;
    private String title;
    private String subtitle;
    private String url;
}

