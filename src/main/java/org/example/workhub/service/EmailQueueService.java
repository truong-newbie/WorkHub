package org.example.workhub.service;

import org.example.workhub.domain.dto.response.EmailQueueProcessResponse;
import org.example.workhub.domain.entity.Subscriber;

import java.time.LocalDateTime;

public interface EmailQueueService {

    boolean enqueueSubscriberMatchingEmail(Subscriber subscriber, String subject, String body, LocalDateTime matchedUntilAt);

    EmailQueueProcessResponse processPendingEmails();
}
