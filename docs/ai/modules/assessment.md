# Assessment Module

## Purpose

Handles recruiter-created tests, questions, options, test publishing, assignment to applications, candidate test taking, answer submission, auto-scoring for multiple choice, and recruiter essay scoring.

## Main Files

- `controller/RecruiterAssessmentController.java`
- `controller/CandidateAssessmentController.java`
- `service/AssessmentTestService.java`
- `service/CandidateAssessmentService.java`
- `service/impl/AssessmentTestServiceImpl.java`
- `service/impl/CandidateAssessmentServiceImpl.java`
- assessment entities under `domain.entity`
- assessment repositories under `repository`

## Business Rules

- Recruiter/admin creates tests for owned jobs.
- Tests must have valid questions/options before publishing.
- Published tests can be assigned to eligible non-rejected applications.
- Candidate can only access own assignments.
- Test duration and time window are enforced.
- Multiple choice answers are auto-scored.
- Essay answers require recruiter scoring.

## Security

- Recruiter endpoints under `/api/v1/recruiter/**` require recruiter/admin.
- Candidate endpoints under `/api/v1/candidate/**` require authentication and ownership checks.

## AI Notes

Do not expose correct options to candidates. Preserve existing separation between candidate and recruiter response DTOs.
