package org.example.workhub.service;

import org.example.workhub.constant.NotificationTargetType;
import org.example.workhub.constant.NotificationType;
import org.example.workhub.domain.dto.pagination.PaginationResponseDto;
import org.example.workhub.domain.dto.request.NotificationSearchRequest;
import org.example.workhub.domain.dto.response.NotificationCountResponse;
import org.example.workhub.domain.dto.response.NotificationResponse;
import org.example.workhub.domain.entity.User;

public interface NotificationService {

    NotificationResponse createAndSend(
            User recipient,
            User sender,
            NotificationType type,
            String title,
            String content,
            NotificationTargetType targetType,
            String targetId
    );

    PaginationResponseDto<NotificationResponse> getMyNotifications(NotificationSearchRequest request);

    NotificationCountResponse getUnreadCount();

    NotificationResponse markAsRead(Long id);

    void markAllAsRead();

    void delete(Long id);
}
