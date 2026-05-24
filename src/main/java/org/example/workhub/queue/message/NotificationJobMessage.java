package org.example.workhub.queue.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationJobMessage {

    private String eventId;
    private String recipientId;
    private String senderId;
    private String type;
    private String title;
    private String content;
    private String targetType;
    private String targetId;
    private LocalDateTime createdAt;
}
