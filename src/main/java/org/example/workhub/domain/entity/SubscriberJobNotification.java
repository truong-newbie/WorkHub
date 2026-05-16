package org.example.workhub.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.workhub.constant.SubscriberJobNotificationStatus;
import org.example.workhub.domain.entity.common.DateAuditing;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "tbl_subscriber_job_notifications",
        indexes = {
                @Index(name = "idx_subscriber_job_notification_subscriber", columnList = "subscriber_id"),
                @Index(name = "idx_subscriber_job_notification_job", columnList = "job_id"),
                @Index(name = "idx_subscriber_job_notification_status", columnList = "status")
        }
)
@Getter
@Setter
public class SubscriberJobNotification extends DateAuditing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscriber_id", nullable = false)
    private Subscriber subscriber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email_queue_id")
    private EmailQueue emailQueue;

    @Column(name = "email", nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriberJobNotificationStatus status = SubscriberJobNotificationStatus.PENDING;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
}
