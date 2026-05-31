# Recommendation Module

## Purpose

Provides personalized job feed using candidate preferences, content scoring, behavior scoring, and collaborative scoring.

## Main Files

- `controller/CandidateJobPreferenceController.java`
- `controller/JobRecommendationController.java`
- `controller/JobBehaviorController.java`
- `service/CandidateJobPreferenceService.java`
- `service/JobRecommendationService.java`
- `service/JobBehaviorService.java`
- `config/RecommendationProperties.java`
- recommendation behavior entities and repositories

## Key APIs

- `GET /api/v1/jobs/latest`
- `GET /api/v1/candidate/onboarding-status`
- `POST /api/v1/candidate/job-preference`
- `GET /api/v1/candidate/job-preference`
- `PUT /api/v1/candidate/job-preference`
- `GET /api/v1/candidate/jobs/recommended`
- `POST /api/v1/candidate/jobs/{jobId}/view`
- `POST /api/v1/candidate/jobs/{jobId}/click`
- `POST /api/v1/candidate/jobs/search-track`
- `GET /api/v1/candidate/recommendation/behavior-summary`

## Scoring

Hybrid score:

```text
contentScore * 0.50 + behaviorScore * 0.30 + collaborativeScore * 0.20
```

Weights are configurable through `recommendation.*` properties.

## Exclusions

Recommendations exclude already-applied jobs, expired jobs, deleted jobs, unpublished jobs, and jobs from inactive/unverified companies.

## AI Notes

When editing scoring, preserve reason codes and response fields because frontend can depend on them.
