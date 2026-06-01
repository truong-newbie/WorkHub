package org.example.workhub.queue.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.workhub.constant.ErrorMessage;
import org.example.workhub.domain.dto.response.ScreeningResultResponse;
import org.example.workhub.queue.message.AtsScreeningJobMessage;
import org.example.workhub.service.ScreeningService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AtsScreeningQueueConsumer {

    private final ScreeningService screeningService;

    @Transactional
    @RabbitListener(queues = "${workhub.queue.ats-screening-queue:ats.screening.queue}", containerFactory = "rabbitListenerContainerFactory")
    public void consume(AtsScreeningJobMessage message) {
        if (message == null || message.getEventId() == null || message.getApplicationId() == null) {
            throw new IllegalArgumentException(ErrorMessage.Queue.ERR_INVALID_MESSAGE);
        }

        ScreeningResultResponse result = screeningService.processQueuedScreening(message.getApplicationId());
        log.info("ATS screening completed eventId={} applicationId={} resumeId={} jobId={} screeningResultId={}",
                message.getEventId(), message.getApplicationId(), message.getResumeId(), message.getJobId(), result.getId());
    }
}
