package org.example.workhub.domain.mapper;

import org.example.workhub.domain.dto.response.NotificationResponse;
import org.example.workhub.domain.entity.Notification;
import org.example.workhub.domain.entity.User;
import org.mapstruct.Mapper;

import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    default NotificationResponse toResponse(Notification notification) {
        if (notification == null) {
            return null;
        }
        User sender = notification.getSender();
        User recipient = notification.getRecipient();
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .content(notification.getContent())
                .targetType(notification.getTargetType())
                .targetId(notification.getTargetId())
                .read(notification.getRead())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .senderId(sender != null ? sender.getId() : null)
                .senderName(sender != null ? sender.getUsername() : null)
                .recipientId(recipient != null ? recipient.getId() : null)
                .build();
    }

    default List<NotificationResponse> toResponses(List<Notification> notifications) {
        if (notifications == null) {
            return Collections.emptyList();
        }
        return notifications.stream().map(this::toResponse).toList();
    }
}
