# Elasticsearch Module

## Purpose

Provides full-text job search index backed by Elasticsearch while MySQL remains source of truth.

## Main Files

- `controller/JobSearchController.java`
- `service/JobSearchService.java`
- `service/impl/JobSearchServiceImpl.java`
- `domain.document.JobSearchDocument.java`
- `domain.mapper.JobSearchMapper.java`
- `listener/JobIndexEventListener.java`
- `config/ElasticsearchConfig.java`
- `elasticsearch/job-settings.json`

## Key APIs

- `GET /api/v1/jobs/search`
- `GET /api/v1/jobs/search/autocomplete`
- `POST /api/v1/jobs/search/reindex`

## Index

```text
workhub_jobs
```

## Sync Flow

Job/company/skill changes publish events. `JobIndexEventListener` updates the Elasticsearch index after commit.

## Fallback

If Elasticsearch is unavailable and fallback is enabled, search can fallback to JPA.

Config:

```properties
search.job.fallback-to-jpa=true
```

## AI Notes

Do not treat Elasticsearch as source of truth. Any write behavior must update MySQL first.
