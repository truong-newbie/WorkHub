package org.example.workhub.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.workhub.base.RestApiV1;
import org.example.workhub.base.VsResponseUtil;
import org.example.workhub.constant.UrlConstant;
import org.example.workhub.domain.dto.request.NotificationSearchRequest;
import org.example.workhub.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
@RestApiV1
public class NotificationController {

    NotificationService notificationService;

    @GetMapping(UrlConstant.Notification.NOTIFICATION_BASE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyNotifications(@ModelAttribute NotificationSearchRequest request) {
        return VsResponseUtil.success(notificationService.getMyNotifications(request));
    }

    @GetMapping(UrlConstant.Notification.UNREAD_COUNT)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUnreadCount() {
        return VsResponseUtil.success(notificationService.getUnreadCount());
    }

    @PutMapping(UrlConstant.Notification.MARK_READ)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        return VsResponseUtil.success(notificationService.markAsRead(id));
    }

    @PutMapping(UrlConstant.Notification.MARK_ALL_READ)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> markAllAsRead() {
        notificationService.markAllAsRead();
        return VsResponseUtil.success(null);
    }

    @DeleteMapping(UrlConstant.Notification.ID)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        notificationService.delete(id);
        return VsResponseUtil.success(null);
    }
}
