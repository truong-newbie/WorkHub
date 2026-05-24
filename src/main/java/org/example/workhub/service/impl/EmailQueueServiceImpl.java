package org.example.workhub.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.workhub.constant.EmailQueueStatus;
import org.example.workhub.constant.ErrorMessage;
import org.example.workhub.constant.SubscriberJobNotificationStatus;
import org.example.workhub.domain.dto.common.MailBody;
import org.example.workhub.domain.dto.response.EmailQueueProcessResponse;
import org.example.workhub.domain.entity.EmailQueue;
import org.example.workhub.domain.entity.Job;
import org.example.workhub.domain.entity.Subscriber;
import org.example.workhub.domain.entity.SubscriberJobNotification;
import org.example.workhub.repository.EmailQueueRepository;
import org.example.workhub.repository.SubscriberJobNotificationRepository;
import org.example.workhub.repository.SubscriberRepository;
import org.example.workhub.queue.message.EmailJobMessage;
import org.example.workhub.queue.producer.BackgroundJobProducer;
import org.example.workhub.service.EmailQueueService;
import org.example.workhub.service.EmailService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EmailQueueServiceImpl implements EmailQueueService {

    private static final Set<EmailQueueStatus> ACTIVE_STATUSES = Set.of(EmailQueueStatus.PENDING, EmailQueueStatus.PROCESSING);

    private final EmailQueueRepository emailQueueRepository;
    private final SubscriberJobNotificationRepository subscriberJobNotificationRepository;
    private final SubscriberRepository subscriberRepository;
    private final EmailService emailService;
    private final BackgroundJobProducer backgroundJobProducer;

    @Override
    public boolean enqueueSubscriberMatchingEmail(
            Subscriber subscriber,
            List<Job> jobs,
            String subject,
            String body,
            Boolean isHtml,
            LocalDateTime matchedUntilAt
    ) {
        if (emailQueueRepository.existsBySubscriberIdAndStatusIn(subscriber.getId(), ACTIVE_STATUSES)) {
            return false;
        }
        if (jobs == null || jobs.isEmpty()) {
            return false;
        }

        EmailQueue emailQueue = new EmailQueue();
        emailQueue.setEventId(UUID.randomUUID().toString());
        emailQueue.setToEmail(subscriber.getEmail());
        emailQueue.setSubject(subject);
        emailQueue.setBody(body);
        emailQueue.setIsHtml(Boolean.TRUE.equals(isHtml));
        emailQueue.setStatus(EmailQueueStatus.PENDING);
        emailQueue.setRetryCount(0);
        emailQueue.setMaxRetry(3);
        emailQueue.setNextAttemptAt(LocalDateTime.now());
        emailQueue.setMatchedUntilAt(matchedUntilAt);
        emailQueue.setSubscriber(subscriber);

        EmailQueue savedQueue = emailQueueRepository.save(emailQueue);
        createPendingNotifications(subscriber, savedQueue, jobs);
        EmailJobMessage message = buildEmailJobMessage(savedQueue, subscriber, jobs);
        publishAfterCommit(savedQueue.getId(), savedQueue.getEventId(), message);
        return true;
    }

    @Override
    public EmailQueueProcessResponse processPendingEmails() {
        List<EmailQueue> queues = emailQueueRepository.findReadyToProcess(
                List.of(EmailQueueStatus.PENDING),
                LocalDateTime.now(),
                PageRequest.of(0, 50)
        );

        int sentEmails = 0;
        int retriedEmails = 0;
        int failedEmails = 0;

        for (EmailQueue queue : queues) {
            try {
                ensureEventId(queue);
                EmailJobMessage message = buildEmailJobMessage(queue);
                publishAfterCommit(queue.getId(), queue.getEventId(), message);
                retriedEmails++;
            } catch (RuntimeException ex) {
                queue.setErrorMessage(ex.getMessage());
                emailQueueRepository.save(queue);
                failedEmails++;
            }
        }

        return EmailQueueProcessResponse.builder()
                .checkedEmails(queues.size())
                .sentEmails(sentEmails)
                .retriedEmails(retriedEmails)
                .failedEmails(failedEmails)
                .build();
    }

    @Override
    public void processEmailJob(EmailJobMessage message) {
        if (message == null || message.getEventId() == null || message.getEventId().isBlank()) {
            throw new IllegalArgumentException(ErrorMessage.Queue.ERR_INVALID_MESSAGE);
        }

        EmailQueue queue = emailQueueRepository.findByEventId(message.getEventId())
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessage.Queue.ERR_JOB_NOT_FOUND));
        if (EmailQueueStatus.SENT.equals(queue.getStatus())) {
            log.info("Skip already sent email queue eventId={}", message.getEventId());
            return;
        }
        if (EmailQueueStatus.FAILED.equals(queue.getStatus()) || EmailQueueStatus.DEAD_LETTER.equals(queue.getStatus())) {
            log.warn("Skip failed email queue eventId={} status={}", message.getEventId(), queue.getStatus());
            return;
        }

        queue.setStatus(EmailQueueStatus.PROCESSING);
        emailQueueRepository.save(queue);

        try {
            MailBody mailBody = MailBody.builder()
                    .to(message.getTo() != null ? message.getTo() : queue.getToEmail())
                    .subject(message.getSubject() != null ? message.getSubject() : queue.getSubject())
                    .text(message.getContent() != null ? message.getContent() : queue.getBody())
                    .build();
            if (Boolean.TRUE.equals(queue.getIsHtml()) || message.getTemplateCode() != null) {
                emailService.sendHtmlMessage(mailBody);
            } else {
                emailService.sendSimpleMessage(mailBody);
            }
            markSent(queue);
        } catch (RuntimeException ex) {
            boolean failed = markRetryOrFailed(queue, ex);
            if (failed) {
                queue.setStatus(EmailQueueStatus.DEAD_LETTER);
                emailQueueRepository.save(queue);
            }
            throw ex;
        }
    }

    private EmailJobMessage buildEmailJobMessage(EmailQueue queue, Subscriber subscriber, List<Job> jobs) {
        ensureEventId(queue);
        return EmailJobMessage.builder()
                .eventId(queue.getEventId())
                .to(queue.getToEmail())
                .subject(queue.getSubject())
                .content(queue.getBody())
                .templateCode(Boolean.TRUE.equals(queue.getIsHtml()) ? "subscriber-job-matching" : null)
                .subscriberId(subscriber.getId())
                .userId(subscriber.getUser() != null ? subscriber.getUser().getId() : null)
                .jobIds(jobs.stream().map(Job::getId).toList())
                .createdAt(LocalDateTime.now())
                .build();
    }

    private EmailJobMessage buildEmailJobMessage(EmailQueue queue) {
        ensureEventId(queue);
        List<SubscriberJobNotification> notifications = subscriberJobNotificationRepository.findByEmailQueueId(queue.getId());
        Subscriber subscriber = queue.getSubscriber();
        return EmailJobMessage.builder()
                .eventId(queue.getEventId())
                .to(queue.getToEmail())
                .subject(queue.getSubject())
                .content(queue.getBody())
                .templateCode(Boolean.TRUE.equals(queue.getIsHtml()) ? "subscriber-job-matching" : null)
                .subscriberId(subscriber != null ? subscriber.getId() : null)
                .userId(subscriber != null && subscriber.getUser() != null ? subscriber.getUser().getId() : null)
                .jobIds(notifications.stream()
                        .filter(notification -> notification.getJob() != null)
                        .map(notification -> notification.getJob().getId())
                        .toList())
                .createdAt(LocalDateTime.now())
                .build();
    }

    private void ensureEventId(EmailQueue queue) {
        if (queue.getEventId() != null && !queue.getEventId().isBlank()) {
            return;
        }
        queue.setEventId(UUID.randomUUID().toString());
        emailQueueRepository.save(queue);
    }

    private void publishAfterCommit(Long queueId, String eventId, EmailJobMessage message) {
        Runnable publish = () -> {
            try {
                backgroundJobProducer.publishEmailJob(message);
            } catch (RuntimeException ex) {
                markQueuePublishFailed(queueId, ex.getMessage());
                log.error("Failed to publish subscriber email job eventId={}", eventId, ex);
            }
        };
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            publish.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                publish.run();
            }
        });
    }

    private void markQueuePublishFailed(Long queueId, String errorMessage) {
        emailQueueRepository.findById(queueId).ifPresent(queue -> {
            queue.setStatus(EmailQueueStatus.FAILED);
            queue.setErrorMessage(errorMessage);
            emailQueueRepository.save(queue);
            markNotificationsFailed(queue, errorMessage);
        });
    }

    private void markSent(EmailQueue queue) {
        LocalDateTime now = LocalDateTime.now();
        queue.setStatus(EmailQueueStatus.SENT);
        queue.setSentAt(now);
        queue.setErrorMessage(null);
        markNotificationsSent(queue, now);

        Subscriber subscriber = queue.getSubscriber();
        if (subscriber != null && queue.getMatchedUntilAt() != null) {
            subscriber.setLastEmailSentAt(queue.getMatchedUntilAt());
            subscriberRepository.save(subscriber);
        }
        emailQueueRepository.save(queue);
    }

    private boolean markRetryOrFailed(EmailQueue queue, RuntimeException ex) {
        int retryCount = queue.getRetryCount() == null ? 0 : queue.getRetryCount();
        int nextRetryCount = retryCount + 1;
        queue.setRetryCount(nextRetryCount);
        queue.setErrorMessage(ex.getMessage());

        if (nextRetryCount >= queue.getMaxRetry()) {
            queue.setStatus(EmailQueueStatus.FAILED);
            markNotificationsFailed(queue, ex.getMessage());
            emailQueueRepository.save(queue);
            return true;
        }

        queue.setStatus(EmailQueueStatus.PENDING);
        queue.setNextAttemptAt(LocalDateTime.now().plusMinutes(nextRetryCount * 5L));
        emailQueueRepository.save(queue);
        return false;
    }

    private void createPendingNotifications(Subscriber subscriber, EmailQueue emailQueue, List<Job> jobs) {
        List<SubscriberJobNotification> notifications = jobs.stream()
                .filter(job -> !subscriberJobNotificationRepository.existsBySubscriberIdAndJobIdAndStatusIn(
                        subscriber.getId(),
                        job.getId(),
                        List.of(SubscriberJobNotificationStatus.SENT, SubscriberJobNotificationStatus.PENDING)
                ))
                .map(job -> {
                    SubscriberJobNotification notification = new SubscriberJobNotification();
                    notification.setSubscriber(subscriber);
                    notification.setJob(job);
                    notification.setEmailQueue(emailQueue);
                    notification.setEmail(subscriber.getEmail());
                    notification.setStatus(SubscriberJobNotificationStatus.PENDING);
                    return notification;
                })
                .toList();
        subscriberJobNotificationRepository.saveAll(notifications);
    }

    private void markNotificationsSent(EmailQueue queue, LocalDateTime sentAt) {
        List<SubscriberJobNotification> notifications = subscriberJobNotificationRepository.findByEmailQueueId(queue.getId());
        notifications.forEach(notification -> {
            notification.setStatus(SubscriberJobNotificationStatus.SENT);
            notification.setSentAt(sentAt);
            notification.setFailedAt(null);
            notification.setErrorMessage(null);
        });
        subscriberJobNotificationRepository.saveAll(notifications);
    }

    private void markNotificationsFailed(EmailQueue queue, String errorMessage) {
        LocalDateTime failedAt = LocalDateTime.now();
        List<SubscriberJobNotification> notifications = subscriberJobNotificationRepository.findByEmailQueueId(queue.getId());
        notifications.forEach(notification -> {
            notification.setStatus(SubscriberJobNotificationStatus.FAILED);
            notification.setFailedAt(failedAt);
            notification.setErrorMessage(errorMessage);
        });
        subscriberJobNotificationRepository.saveAll(notifications);
    }
}
