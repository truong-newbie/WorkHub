# Job Module

## Purpose

Handles job CRUD, publish/unpublish, statistics, favorite jobs, job applications, recruiter applicant views, and candidate apply/withdraw flows.

## Main Files

- `controller/JobController.java`
- `controller/JobApplicationController.java`
- `controller/FavoriteJobController.java`
- `service/JobService.java`
- `service/JobApplicationService.java`
- `service/FavoriteJobService.java`
- `domain.entity.Job.java`
- `domain.entity.JobApplication.java`
- `domain.entity.FavoriteJob.java`
- `repository/JobRepository.java`
- `repository/JobApplicationRepository.java`

## Key APIs

- `/api/v1/job`
- `/api/v1/job/{id}`
- `/api/v1/job/{id}/publish`
- `/api/v1/job/{id}/unpublish`
- `/api/v1/job/{jobId}/apply`
- `/api/v1/job/{jobId}/applications`
- `/api/v1/applications/{applicationId}/status`
- `/api/v1/recruiter/applications/{applicationId}/screen`

## Business Rules

- Jobs must be published, not deleted, and not expired before candidate apply.
- Candidate cannot apply twice to the same job.
- Recruiter/admin can view applicants only with proper ownership/admin access.
- Application status update endpoint allows recruiter review statuses only.

## AI Notes

For ATS screening, also load `modules/ats.md` and `modules/queue-system.md`.
