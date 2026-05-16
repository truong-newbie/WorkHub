package org.example.workhub.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.workhub.constant.EmailQueueStatus;
import org.example.workhub.domain.dto.common.MailBody;
import org.example.workhub.domain.dto.response.EmailQueueProcessResponse;
import org.example.workhub.domain.entity.EmailQueue;
import org.example.workhub.domain.entity.Subscriber;
import org.example.workhub.repository.EmailQueueRepository;
import org.example.workhub.repository.SubscriberRepository;
import org.example.workhub.service.EmailQueueService;
import org.example.workhub.service.EmailService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EmailQueueServiceImpl implements EmailQueueService {

    private static final Set<EmailQueueStatus> ACTIVE_STATUSES = Set.of(EmailQueueStatus.PENDING, EmailQueueStatus.PROCESSING);

    private final EmailQueueRepository emailQueueRepository;
    private final SubscriberRepository subscriberRepository;
    private final EmailService emailService;

    @Override
    public boolean enqueueSubscriberMatchingEmail(Subscriber subscriber, String subject, String body, LocalDateTime matchedUntilAt) {
        if (emailQueueRepository.existsBySubscriberIdAndStatusIn(subscriber.getId(), ACTIVE_STATUSES)) {
            return false;
        }

        EmailQueue emailQueue = new EmailQueue();
        emailQueue.setToEmail(subscriber.getEmail());
        emailQueue.setSubject(subject);
        emailQueue.setBody(body);
        emailQueue.setStatus(EmailQueueStatus.PENDING);
        emailQueue.setRetryCount(0);
        emailQueue.setMaxRetry(3);
        emailQueue.setNextAttemptAt(LocalDateTime.now());
        emailQueue.setMatchedUntilAt(matchedUntilAt);
        emailQueue.setSubscriber(subscriber);

        emailQueueRepository.save(emailQueue);
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
            queue.setStatus(EmailQueueStatus.PROCESSING);
            emailQueueRepository.save(queue);

            try {
                emailService.sendSimpleMessage(MailBody.builder()
                        .to(queue.getToEmail())
                        .subject(queue.getSubject())
                        .text(queue.getBody())
                        .build());

                markSent(queue);
                sentEmails++;
            } catch (RuntimeException ex) {
                if (markRetryOrFailed(queue, ex)) {
                    failedEmails++;
                } else {
                    retriedEmails++;
                }
            }
        }

        return EmailQueueProcessResponse.builder()
                .checkedEmails(queues.size())
                .sentEmails(sentEmails)
                .retriedEmails(retriedEmails)
                .failedEmails(failedEmails)
                .build();
    }

    private void markSent(EmailQueue queue) {
        LocalDateTime now = LocalDateTime.now();
        queue.setStatus(EmailQueueStatus.SENT);
        queue.setSentAt(now);
        queue.setErrorMessage(null);

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
            emailQueueRepository.save(queue);
            return true;
        }

        queue.setStatus(EmailQueueStatus.PENDING);
        queue.setNextAttemptAt(LocalDateTime.now().plusMinutes(nextRetryCount * 5L));
        emailQueueRepository.save(queue);
        return false;
    }
}
