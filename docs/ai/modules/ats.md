# ATS Screening Module

## Purpose

Provides asynchronous ATS screening for recruiter/admin to screen an application against the candidate resume and read persisted results.

## Current Source State

The ATS flow is implemented end to end. The backend queues work through RabbitMQ, the consumer calls the AI worker, persists `ScreeningResult`, updates the application to `SCREENED`, and emits a completion notification.

The AI worker calculates keyword skill matching and semantic similarity with the local embedding model:

```text
sentence-transformers/all-MiniLM-L6-v2
```

`semantic_score` is cosine similarity converted to a value from `0` to `100`.
The combined score is:

```text
final_score = skill_score * 0.60 + semantic_score * 0.40
```

Queue requests are not deduplicated before publishing. Re-screening updates the existing `ScreeningResult` row for the application.

## Main Files

- `controller/JobApplicationController.java`
- `service/JobApplicationService.java`
- `service/impl/JobApplicationServiceImpl.java`
- `service/impl/ScreeningServiceImpl.java`
- `queue/message/AtsScreeningJobMessage.java`
- `queue/consumer/AtsScreeningQueueConsumer.java`
- `domain/entity/ScreeningResult.java`
- `ai-worker/app/main.py`
- `ai-worker/app/services/analysis_service.py`
- `ai-worker/app/services/embedding_service.py`
- `ai-worker/app/services/scoring_service.py`

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

`ai-worker/app/main.py` accepts the resume file, `job_description`, and optional comma-separated `required_skills`. It parses the resume and delegates scoring to focused services.

`embedding_service.py` lazy-loads one model instance per worker process, normalizes whitespace and Unicode, and truncates embedded input to `8000` characters by default.

Environment overrides:

```text
AI_WORKER_EMBEDDING_MODEL
AI_WORKER_MAX_TEXT_LENGTH
```

Dependencies:

```text
sentence-transformers
scikit-learn
numpy
```

The Docker image installs CPU-only PyTorch because ATS inference does not require CUDA. Docker Compose mounts `ai_worker_model_cache` at `/root/.cache/huggingface` so the model download is reused after container recreation.

The AI worker returns HTTP `422` when parsed resume text or job text is empty and HTTP `503` when the embedding model cannot load or encode. It must not silently return a fake semantic score.

Run locally:

```text
cd ai-worker
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8000
```

Run tests:

```text
python -m unittest discover -s tests -v
RUN_EMBEDDING_INTEGRATION_TESTS=true python -m unittest tests.test_embedding_service.RealEmbeddingIntegrationTest -v
```
