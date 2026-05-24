package org.example.workhub.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.workhub.constant.ErrorMessage;
import org.example.workhub.constant.NotificationTargetType;
import org.example.workhub.constant.NotificationType;
import org.example.workhub.constant.SortByDataConstant;
import org.example.workhub.domain.dto.pagination.PaginationResponseDto;
import org.example.workhub.domain.dto.pagination.PagingMeta;
import org.example.workhub.domain.dto.request.NotificationSearchRequest;
import org.example.workhub.domain.dto.response.NotificationCountResponse;
import org.example.workhub.domain.dto.response.NotificationResponse;
import org.example.workhub.domain.entity.Notification;
import org.example.workhub.domain.entity.User;
import org.example.workhub.domain.mapper.NotificationMapper;
import org.example.workhub.domain.specification.NotificationSpecification;
import org.example.workhub.exception.ForbiddenException;
import org.example.workhub.exception.NotFoundException;
import org.example.workhub.repository.NotificationRepository;
import org.example.workhub.repository.UserRepository;
import org.example.workhub.security.UserPrincipal;
import org.example.workhub.service.NotificationService;
import org.example.workhub.util.PaginationUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public NotificationResponse createAndSend(User recipient,
                                              User sender,
                                              NotificationType type,
                                              String title,
                                              String content,
                                              NotificationTargetType targetType,
                                              String targetId) {
        if (recipient == null || recipient.getId() == null) {
            return null;
        }
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setSender(sender);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setTargetType(targetType == null ? NotificationTargetType.NONE : targetType);
        notification.setTargetId(targetId);
        notification.setRead(false);
        notification.setDeleted(false);

        NotificationResponse response = notificationMapper.toResponse(notificationRepository.save(notification));
        messagingTemplate.convertAndSendToUser(recipient.getId(), "/queue/notifications", response);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponseDto<NotificationResponse> getMyNotifications(NotificationSearchRequest request) {
        UserPrincipal currentUser = getCurrentUserPrincipal();
        Pageable pageable = PaginationUtil.buildPageable(request, SortByDataConstant.NOTIFICATION);
        Page<Notification> page = notificationRepository.findAll(
                NotificationSpecification.withFilters(request, currentUser.getId()),
                pageable
        );
        PagingMeta meta = PaginationUtil.buildPagingMeta(request, SortByDataConstant.NOTIFICATION, page);
        return new PaginationResponseDto<>(meta, notificationMapper.toResponses(page.getContent()));
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationCountResponse getUnreadCount() {
        UserPrincipal currentUser = getCurrentUserPrincipal();
        return new NotificationCountResponse(notificationRepository.countByRecipientIdAndReadFalseAndDeletedFalse(currentUser.getId()));
    }

    @Override
    public NotificationResponse markAsRead(Long id) {
        UserPrincipal currentUser = getCurrentUserPrincipal();
        Notification notification = notificationRepository.findByIdAndRecipientIdAndDeletedFalse(id, currentUser.getId())
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Notification.ERR_NOT_FOUND_ID, new String[]{String.valueOf(id)}));
        if (notification.getRecipient() == null || !currentUser.getId().equals(notification.getRecipient().getId())) {
            throw new ForbiddenException(ErrorMessage.Notification.ERR_FORBIDDEN_ACCESS);
        }
        if (!Boolean.TRUE.equals(notification.getRead())) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
        }
        return notificationMapper.toResponse(notificationRepository.save(notification));
    }

    @Override
    public void markAllAsRead() {
        UserPrincipal currentUser = getCurrentUserPrincipal();
        notificationRepository.markAllAsRead(currentUser.getId(), LocalDateTime.now());
    }

    @Override
    public void delete(Long id) {
        UserPrincipal currentUser = getCurrentUserPrincipal();
        Notification notification = notificationRepository.findByIdAndRecipientIdAndDeletedFalse(id, currentUser.getId())
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Notification.ERR_NOT_FOUND_ID, new String[]{String.valueOf(id)}));
        notification.setDeleted(true);
        notificationRepository.save(notification);
    }

    private UserPrincipal getCurrentUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken
                || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new ForbiddenException(ErrorMessage.UNAUTHORIZED);
        }
        userRepository.findById(principal.getId())
                .filter(user -> !Boolean.TRUE.equals(user.getDeleted()))
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{principal.getId()}));
        return principal;
    }
}
