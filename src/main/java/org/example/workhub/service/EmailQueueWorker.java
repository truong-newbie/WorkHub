package org.example.workhub.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "email.queue.worker.enabled", havingValue = "true")
public class EmailQueueWorker {

    private final EmailQueueService emailQueueService;

    @Scheduled(
            initialDelayString = "${email.queue.worker.initial-delay-ms:30000}",
            fixedDelayString = "${email.queue.worker.delay-ms:60000}"
    )
    public void processPendingEmails() {
        emailQueueService.processPendingEmails();
    }
}
