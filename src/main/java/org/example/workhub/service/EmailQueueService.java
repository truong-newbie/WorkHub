package org.example.workhub.service;

import org.example.workhub.domain.dto.response.EmailQueueProcessResponse;
import org.example.workhub.domain.entity.Job;
import org.example.workhub.domain.entity.Subscriber;

import java.time.LocalDateTime;
import java.util.List;

public interface EmailQueueService {

    boolean enqueueSubscriberMatchingEmail(
            Subscriber subscriber,
            List<Job> jobs,
            String subject,
            String body,
            Boolean isHtml,
            LocalDateTime matchedUntilAt
    );

    EmailQueueProcessResponse processPendingEmails();
}
