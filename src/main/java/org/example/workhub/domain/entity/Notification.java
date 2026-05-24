package org.example.workhub.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.workhub.constant.NotificationTargetType;
import org.example.workhub.constant.NotificationType;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "tbl_notifications",
        indexes = {
                @Index(name = "idx_notification_recipient", columnList = "recipient_id"),
                @Index(name = "idx_notification_read", columnList = "is_read"),
                @Index(name = "idx_notification_created_at", columnList = "created_at")
        }
)
@Getter
@Setter
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 80)
    private NotificationType type;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 80)
    private NotificationTargetType targetType = NotificationTargetType.NONE;

    @Column(name = "target_id")
    private String targetId;

    @Column(name = "is_read", nullable = false)
    private Boolean read = false;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (read == null) {
            read = false;
        }
        if (deleted == null) {
            deleted = false;
        }
        if (targetType == null) {
            targetType = NotificationTargetType.NONE;
        }
    }
}
