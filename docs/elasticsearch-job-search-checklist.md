# Elasticsearch Job Search Checklist

1. Start MySQL, Elasticsearch and backend with `docker compose up -d --build`.
2. Create a job named `Java Developer`.
3. Publish the job.
4. Login as admin and call `POST /api/v1/jobs/search/reindex`.
5. Search `java developer` and confirm the job is returned.
6. Search `jav develper` and confirm `Java Developer` is still returned.
7. Search `spring boot` and confirm matches from requirement, description or skills.
8. Search with `skillIds=1&skillIds=2` and confirm only matching jobs are returned.
9. Search with `location=hanoi` and confirm location matching works.
10. Call autocomplete with `keyword=jav` and confirm job title or skill suggestions are returned.
11. Unpublish the job and confirm search no longer returns it.
12. Soft delete the job and confirm search no longer returns it.
13. Update job title and confirm the index reflects the new title.
14. Confirm `POST /api/v1/jobs/search/reindex` only works for ADMIN.
15. Stop Elasticsearch and confirm create/update/delete job APIs still succeed.
16. With Elasticsearch stopped, confirm search falls back to JPA when `search.job.fallback-to-jpa=true`.
