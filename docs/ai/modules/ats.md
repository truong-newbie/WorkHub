# ATS Screening Module

## Purpose

Provides asynchronous ATS screening request entrypoint for recruiter/admin to screen an application against a candidate resume.

## Current Source State

The codebase currently has an ATS queue endpoint and consumer skeleton. Historical docs mention richer ATS Java services and `ScreeningResult`, but the current source tree does not contain a full Java ATS screening service/entity implementation.

## Main Files

- `controller/JobApplicationController.java`
- `service/JobApplicationService.java`
- `service/impl/JobApplicationServiceImpl.java`
- `queue/message/AtsScreeningJobMessage.java`
- `queue/consumer/AtsScreeningQueueConsumer.java`

## Key API

```text
POST /api/v1/recruiter/applications/{applicationId}/screen
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

Consumer loads application and resume, logs the integration point, and can publish a notification job.

## AI Notes

Before implementing full ATS scoring, inspect actual entities and repositories first. Add dedicated `ScreeningResult` entity/status only when implementing the complete persistence model.
