package org.example.workhub.listener;

import lombok.RequiredArgsConstructor;
import org.example.workhub.constant.NotificationTargetType;
import org.example.workhub.constant.NotificationType;
import org.example.workhub.domain.entity.User;
import org.example.workhub.event.*;
import org.example.workhub.service.NotificationService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onJobApplicationCreated(JobApplicationCreatedEvent event) {
        notificationService.createAndSend(
                event.getRecipient(),
                event.getSender(),
                NotificationType.JOB_APPLICATION_CREATED,
                "New job application",
                event.getCandidateName() + " has applied for " + event.getJobTitle(),
                NotificationTargetType.APPLICATION,
                String.valueOf(event.getApplicationId())
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onJobApplicationStatusUpdated(JobApplicationStatusUpdatedEvent event) {
        notificationService.createAndSend(
                event.getRecipient(),
                event.getSender(),
                NotificationType.JOB_APPLICATION_STATUS_UPDATED,
                "Application status updated",
                "Your application for " + event.getJobTitle() + " has been updated to " + event.getStatus(),
                NotificationTargetType.APPLICATION,
                String.valueOf(event.getApplicationId())
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAssessmentAssigned(AssessmentAssignedEvent event) {
        notificationService.createAndSend(
                event.getRecipient(),
                event.getSender(),
                NotificationType.ASSESSMENT_ASSIGNED,
                "Assessment assigned",
                "You have been assigned an assessment for " + event.getJobTitle(),
                NotificationTargetType.ASSESSMENT,
                String.valueOf(event.getAssignmentId())
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAssessmentSubmitted(AssessmentSubmittedEvent event) {
        notificationService.createAndSend(
                event.getRecipient(),
                event.getSender(),
                NotificationType.ASSESSMENT_SUBMITTED,
                "Assessment submitted",
                event.getCandidateName() + " has submitted the assessment for " + event.getJobTitle(),
                NotificationTargetType.ASSESSMENT,
                String.valueOf(event.getAssignmentId())
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCompanyModerated(CompanyModeratedEvent event) {
        NotificationType type = event.isApproved() ? NotificationType.COMPANY_APPROVED : NotificationType.COMPANY_REJECTED;
        String status = event.isApproved() ? "approved" : "rejected";
        String title = event.isApproved() ? "Company approved" : "Company rejected";
        Set<String> notifiedUserIds = new HashSet<>();
        for (User recipient : event.getRecipients()) {
            if (recipient == null || recipient.getId() == null || !notifiedUserIds.add(recipient.getId())) {
                continue;
            }
            notificationService.createAndSend(
                    recipient,
                    event.getSender(),
                    type,
                    title,
                    "Your company " + event.getCompanyName() + " has been " + status,
                    NotificationTargetType.COMPANY,
                    String.valueOf(event.getCompanyId())
            );
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAtsScreeningCompleted(AtsScreeningCompletedEvent event) {
        notificationService.createAndSend(
                event.getRecipient(),
                null,
                NotificationType.ATS_SCREENING_COMPLETED,
                "ATS screening completed",
                "Screening result for " + event.getCandidateName() + " is ready",
                NotificationTargetType.SCREENING_RESULT,
                event.getScreeningResultId()
        );
    }
}
