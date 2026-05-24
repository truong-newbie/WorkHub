# Hybrid Recommendation Engine v2

## 1. Goal

Upgrade WorkHub candidate job recommendations from the original content-only MVP to a hybrid engine that combines candidate preference, candidate behavior and collaborative signals from similar users.

## 2. Architecture

The v2 endpoint stays on:

```text
GET /api/v1/candidate/jobs/recommended
```

The engine uses MySQL as the source of truth and computes recommendations in real time for the configured candidate pool size.

Layers:

- Content-based scoring from `CandidateJobPreference` and job fields.
- Behavioral scoring from view, click, favorite, application and search history.
- Collaborative filtering from users with similar skills, saved jobs, applied jobs and viewed/clicked jobs.

## 3. Content-Based Scoring

`contentScore` is normalized from 0 to 100:

```text
contentScore =
skillScore * 0.40
+ titleScore * 0.20
+ locationWorkModeScore * 0.15
+ experienceScore * 0.15
+ salaryScore * 0.10
```

The engine returns matched skills, missing skills and content reason codes.

## 4. Behavioral Scoring

The engine builds a candidate interest profile for the configured history window:

- View weight: `recommendation.behavior.view-weight`
- Click weight: `recommendation.behavior.click-weight`
- Save weight: `recommendation.behavior.save-weight`
- Apply weight: `recommendation.behavior.apply-weight`
- Search keyword weight: `recommendation.behavior.search-keyword-weight`

The profile tracks skill, title keyword, location and company interests. Candidate jobs are scored by overlap with those interests and normalized to 0-100 within the current result pool.

## 5. Collaborative Filtering MVP

Similar users are selected by:

```text
similarity =
skillSimilarity * 0.40
+ appliedJobSimilarity * 0.30
+ savedJobSimilarity * 0.20
+ behaviorSimilarity * 0.10
```

Only users above `recommendation.collaborative.min-similarity` are used, limited by `recommendation.collaborative.similar-user-limit`.

Candidate jobs receive collaborative raw score from similar users who applied, saved, viewed or clicked those jobs, then the score is normalized to 0-100.

## 6. Database Tables

New tables:

- `tbl_job_view_histories`
- `tbl_job_click_histories`
- `tbl_job_search_histories`
- `tbl_job_recommendation_logs`

Existing tables reused:

- `tbl_candidate_job_preferences`
- `tbl_favorite_jobs`
- `tbl_job_applications`
- `tbl_jobs`
- `tbl_job_skill`

## 7. API List

```text
POST /api/v1/candidate/jobs/{jobId}/view
POST /api/v1/candidate/jobs/{jobId}/click
POST /api/v1/candidate/jobs/search-track
GET  /api/v1/candidate/jobs/recommended
GET  /api/v1/candidate/recommendation/behavior-summary
```

## 8. Example Response

```json
{
  "status": "SUCCESS",
  "data": {
    "items": [
      {
        "id": 12,
        "jobId": 12,
        "title": "Senior Java Developer",
        "companyName": "WorkHub Labs",
        "location": "Ha Noi",
        "contentScore": 84.5,
        "behaviorScore": 72.0,
        "collaborativeScore": 40.0,
        "hybridScore": 71.9,
        "matchScore": 71.9,
        "matchedSkills": ["Java", "Spring Boot"],
        "missingSkills": ["AWS"],
        "reasonCodes": ["MATCHED_SKILL", "BASED_ON_CLICK_HISTORY", "USERS_LIKE_YOU_APPLIED"],
        "reasonText": "Recommended because you match 2 required skill(s), and your recent behavior points to similar jobs, and similar candidates engaged with this job."
      }
    ]
  }
}
```

## 9. How To Test

1. Create candidate preference with title, skills, location, work mode, level and salary.
2. Create several published, non-deleted and non-expired jobs.
3. Track view/click/search events for one job family.
4. Save and apply jobs through the existing APIs.
5. Create another candidate with overlapping skills and behavior.
6. Call `GET /api/v1/candidate/jobs/recommended?pageNum=1&pageSize=10&explain=true`.
7. Confirm applied jobs are excluded and scores/reason codes change with behavior.

## 10. CV Line

Built a hybrid job recommendation engine using content-based scoring, behavioral personalization and collaborative filtering, with explainable reason codes and configurable scoring weights.
