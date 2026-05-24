package org.example.workhub.queue.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.workhub.constant.ErrorMessage;
import org.example.workhub.constant.NotificationTargetType;
import org.example.workhub.constant.NotificationType;
import org.example.workhub.domain.entity.User;
import org.example.workhub.queue.message.NotificationJobMessage;
import org.example.workhub.repository.UserRepository;
import org.example.workhub.service.NotificationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationQueueConsumer {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @Transactional
    @RabbitListener(queues = "${workhub.queue.notification-queue:notification.queue}", containerFactory = "rabbitListenerContainerFactory")
    public void consume(NotificationJobMessage message) {
        if (message == null || message.getEventId() == null || message.getRecipientId() == null) {
            throw new IllegalArgumentException(ErrorMessage.Queue.ERR_INVALID_MESSAGE);
        }

        User recipient = userRepository.findById(message.getRecipientId())
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessage.User.ERR_NOT_FOUND_ID));
        User sender = message.getSenderId() == null ? null : userRepository.findById(message.getSenderId()).orElse(null);
        NotificationType type = parseType(message.getType());
        NotificationTargetType targetType = parseTargetType(message.getTargetType());

        notificationService.createAndSend(
                recipient,
                sender,
                type,
                message.getTitle(),
                message.getContent(),
                targetType,
                message.getTargetId()
        );
        log.info("Notification job consumed eventId={} recipientId={}", message.getEventId(), message.getRecipientId());
    }

    private NotificationType parseType(String type) {
        if (type == null || type.isBlank()) {
            return NotificationType.SYSTEM;
        }
        return NotificationType.valueOf(type);
    }

    private NotificationTargetType parseTargetType(String targetType) {
        if (targetType == null || targetType.isBlank()) {
            return NotificationTargetType.NONE;
        }
        return NotificationTargetType.valueOf(targetType);
    }
}
