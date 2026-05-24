package org.example.workhub.queue.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.workhub.constant.ErrorMessage;
import org.example.workhub.exception.InternalServerException;
import org.example.workhub.queue.config.QueueRoutingKeys;
import org.example.workhub.queue.message.AtsScreeningJobMessage;
import org.example.workhub.queue.message.EmailJobMessage;
import org.example.workhub.queue.message.NotificationJobMessage;
import org.example.workhub.queue.message.ResumeParsingJobMessage;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BackgroundJobProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${workhub.queue.exchange:workhub.exchange}")
    private String exchangeName;

    public void publishEmailJob(EmailJobMessage message) {
        publish(QueueRoutingKeys.EMAIL_SEND, message.getEventId(), message);
    }

    public void publishAtsScreeningJob(AtsScreeningJobMessage message) {
        publish(QueueRoutingKeys.ATS_SCREENING_REQUEST, message.getEventId(), message);
    }

    public void publishResumeParsingJob(ResumeParsingJobMessage message) {
        publish(QueueRoutingKeys.RESUME_PARSING_REQUEST, message.getEventId(), message);
    }

    public void publishNotificationJob(NotificationJobMessage message) {
        publish(QueueRoutingKeys.NOTIFICATION_SEND, message.getEventId(), message);
    }

    private void publish(String routingKey, String eventId, Object message) {
        try {
            rabbitTemplate.convertAndSend(exchangeName, routingKey, message);
            log.info("Published background job eventId={} routingKey={}", eventId, routingKey);
        } catch (AmqpException ex) {
            log.error("Failed to publish background job eventId={} routingKey={}", eventId, routingKey, ex);
            throw new InternalServerException(ErrorMessage.Queue.ERR_PUBLISH_FAILED);
        }
    }
}
