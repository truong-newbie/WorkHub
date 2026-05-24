package org.example.workhub.domain.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.workhub.constant.NotificationTargetType;
import org.example.workhub.constant.NotificationType;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class NotificationResponse {

    private Long id;

    private NotificationType type;

    private String title;

    private String content;

    private NotificationTargetType targetType;

    private String targetId;

    private Boolean read;

    private LocalDateTime createdAt;

    private LocalDateTime readAt;

    private String senderId;

    private String senderName;

    private String recipientId;
}
