# ATS Screening Module

## Purpose

Provides asynchronous ATS screening for recruiter/admin to screen an application against the candidate resume and read persisted results.

## Current Source State

The ATS flow is implemented end to end. The backend queues work through RabbitMQ, the consumer calls the AI worker, persists `ScreeningResult`, updates the application to `SCREENED`, and emits a completion notification.

The AI worker currently calculates skill matching. Semantic scoring is still a placeholder returning `0`, and queue requests are not deduplicated before publishing.

## Main Files

- `controller/JobApplicationController.java`
- `service/JobApplicationService.java`
- `service/impl/JobApplicationServiceImpl.java`
- `service/impl/ScreeningServiceImpl.java`
- `queue/message/AtsScreeningJobMessage.java`
- `queue/consumer/AtsScreeningQueueConsumer.java`
- `domain/entity/ScreeningResult.java`
- `ai-worker/app/main.py`

## Key API

```text
POST /api/v1/recruiter/applications/{applicationId}/screen
GET  /api/v1/recruiter/applications/{applicationId}/screening-result
GET  /api/v1/recruiter/jobs/{jobId}/screening-results
```

Expected response:

```json
{
  "applicationId": 10,
  "resumeId": 3,
  "jobId": 5,
  "screeningStatus": "PROCESSING",
  "message": "ATS screening job has been queued",
  "eventId": "..."
}
```

## Security

Requires recruiter/admin and job ownership/admin access.

## Queue Flow

Controller/service validates access and publishes `AtsScreeningJobMessage`.

Consumer calls `ScreeningService.processQueuedScreening`, which analyzes the linked resume, saves the result, changes the application status to `SCREENED`, and publishes `AtsScreeningCompletedEvent`.

## AI Notes

`ai-worker/app/main.py` parses the resume and calculates matched, missing, and extra skills. Extend `semantic_service.py` when semantic scoring is required.
