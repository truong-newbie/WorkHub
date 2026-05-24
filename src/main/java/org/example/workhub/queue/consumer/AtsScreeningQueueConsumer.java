package org.example.workhub.queue.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.workhub.constant.ErrorMessage;
import org.example.workhub.constant.NotificationTargetType;
import org.example.workhub.constant.NotificationType;
import org.example.workhub.domain.entity.JobApplication;
import org.example.workhub.domain.entity.Resume;
import org.example.workhub.domain.entity.User;
import org.example.workhub.queue.message.AtsScreeningJobMessage;
import org.example.workhub.queue.message.NotificationJobMessage;
import org.example.workhub.queue.producer.BackgroundJobProducer;
import org.example.workhub.repository.JobApplicationRepository;
import org.example.workhub.repository.ResumeRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AtsScreeningQueueConsumer {

    private final JobApplicationRepository jobApplicationRepository;
    private final ResumeRepository resumeRepository;
    private final BackgroundJobProducer backgroundJobProducer;

    @Transactional
    @RabbitListener(queues = "${workhub.queue.ats-screening-queue:ats.screening.queue}", containerFactory = "rabbitListenerContainerFactory")
    public void consume(AtsScreeningJobMessage message) {
        if (message == null || message.getEventId() == null || message.getApplicationId() == null) {
            throw new IllegalArgumentException(ErrorMessage.Queue.ERR_INVALID_MESSAGE);
        }

        JobApplication application = jobApplicationRepository.findById(message.getApplicationId())
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessage.Application.ERR_NOT_FOUND_ID));
        Resume resume = resumeRepository.findByIdAndDeletedFalse(message.getResumeId())
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessage.Resume.ERR_NOT_FOUND));

        log.info("ATS screening skeleton consumed eventId={} applicationId={} resumeId={} jobId={}",
                message.getEventId(), application.getId(), resume.getId(), message.getJobId());

        User recipient = application.getJob() != null && application.getJob().getRecruiter() != null
                ? application.getJob().getRecruiter()
                : application.getJob() != null && application.getJob().getCompany() != null
                ? application.getJob().getCompany().getOwner()
                : null;
        if (recipient == null) {
            return;
        }

        try {
            backgroundJobProducer.publishNotificationJob(NotificationJobMessage.builder()
                    .eventId(UUID.randomUUID().toString())
                    .recipientId(recipient.getId())
                    .senderId(null)
                    .type(NotificationType.ATS_SCREENING_COMPLETED.name())
                    .title("ATS screening queued")
                    .content("ATS screening request has been consumed and is ready for the AI worker integration.")
                    .targetType(NotificationTargetType.APPLICATION.name())
                    .targetId(String.valueOf(application.getId()))
                    .createdAt(LocalDateTime.now())
                    .build());
        } catch (RuntimeException ex) {
            log.error("Failed to publish ATS notification eventId={}", message.getEventId(), ex);
        }
    }
}
