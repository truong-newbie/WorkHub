package org.example.workhub.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriberMailScheduler {

    private final SubscriberService subscriberService;

    @Scheduled(cron = "${subscriber.mail.cron:0 0 */6 * * *}")
    public void sendMatchingJobEmails() {
        subscriberService.sendMatchingJobEmails();
    }
}
