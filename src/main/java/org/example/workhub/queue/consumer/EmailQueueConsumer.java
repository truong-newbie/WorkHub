package org.example.workhub.queue.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.workhub.queue.message.EmailJobMessage;
import org.example.workhub.service.EmailQueueService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailQueueConsumer {

    private final EmailQueueService emailQueueService;

    @RabbitListener(queues = "${workhub.queue.email-queue:email.queue}", containerFactory = "rabbitListenerContainerFactory")
    public void consume(EmailJobMessage message) {
        log.info("Consuming email queue message eventId={}", message != null ? message.getEventId() : null);
        emailQueueService.processEmailJob(message);
    }
}
