package org.example.workhub.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.workhub.repository.JobRepository;
import org.example.workhub.event.*;
import org.example.workhub.service.JobSearchService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobIndexEventListener {

    private final JobSearchService jobSearchService;
    private final JobRepository jobRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onJobCreated(JobCreatedEvent event) {
        jobSearchService.indexJob(event.getJobId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onJobUpdated(JobUpdatedEvent event) {
        jobSearchService.indexJob(event.getJobId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onJobDeleted(JobDeletedEvent event) {
        jobSearchService.deleteJob(event.getJobId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onJobPublished(JobPublishedEvent event) {
        jobSearchService.indexJob(event.getJobId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onJobUnpublished(JobUnpublishedEvent event) {
        jobSearchService.indexJob(event.getJobId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCompanyChanged(CompanyChangedEvent event) {
        jobRepository.findIdsByCompanyId(event.getCompanyId())
                .forEach(jobSearchService::indexJob);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSkillChanged(SkillChangedEvent event) {
        jobRepository.findIdsBySkillId(event.getSkillId())
                .forEach(jobSearchService::indexJob);
    }
}
