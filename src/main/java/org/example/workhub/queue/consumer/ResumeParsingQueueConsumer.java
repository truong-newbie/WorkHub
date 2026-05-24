package org.example.workhub.queue.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.workhub.constant.ErrorMessage;
import org.example.workhub.domain.entity.Resume;
import org.example.workhub.queue.message.ResumeParsingJobMessage;
import org.example.workhub.repository.ResumeRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResumeParsingQueueConsumer {

    private final ResumeRepository resumeRepository;

    @Transactional
    @RabbitListener(queues = "${workhub.queue.resume-parsing-queue:resume.parsing.queue}", containerFactory = "rabbitListenerContainerFactory")
    public void consume(ResumeParsingJobMessage message) {
        if (message == null || message.getEventId() == null || message.getResumeId() == null) {
            throw new IllegalArgumentException(ErrorMessage.Queue.ERR_INVALID_MESSAGE);
        }

        Resume resume = resumeRepository.findByIdAndDeletedFalse(message.getResumeId())
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessage.Resume.ERR_NOT_FOUND));
        if (resume.getParsedContent() != null && !resume.getParsedContent().isBlank()) {
            log.info("Skip already parsed resume eventId={} resumeId={}", message.getEventId(), resume.getId());
            return;
        }

        log.info("Resume parsing skeleton consumed eventId={} resumeId={} fileUrl={}",
                message.getEventId(), resume.getId(), message.getFileUrl());
    }
}
