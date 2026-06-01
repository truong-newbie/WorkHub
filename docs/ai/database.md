# WorkHub Database Notes

## Main Tables

- `users`
- `tbl_roles`
- `tbl_permissions`
- `tbl_companies`
- `tbl_skills`
- `tbl_jobs`
- `tbl_job_applications`
- `tbl_resumes`
- `table_subscribers`
- `tbl_email_queues`
- `tbl_notifications`
- assessment tables
- recommendation behavior tables

## ID Types

- `User.id`: `String` UUID.
- Most business entities: `Long`.
- `Company.id`: `Long`, not `String`.

## Auditing

Common base entities provide created/modified fields:

- `DateAuditing`
- `UserDateAuditing`
- `FlagUserDateAuditing`

Do not manually map audit fields unless the existing module requires it.

## Soft Delete

Common flags:

- `deleted`
- `enabled`
- `active`
- `published`
- `verified`

Repository methods and specifications should usually exclude deleted rows.

## Status Enum Caveat

`StatusEnum` is shared by application-like flows and currently includes:

```java
PENDING
REVIEWING
SCREENED
APPROVED
REJECTED
```

Do not reuse it for unrelated background job states. Queue/background jobs should use dedicated enums or explicit queue status fields.

## Background Queue Tables

Existing email queue tracking table:

```text
tbl_email_queues
```

RabbitMQ is the transport. The DB email queue remains the audit/idempotency layer for subscriber emails.

## Database Rule For AI Work

Before using a field, inspect the actual entity class. Do not rely only on old docs because some historical context may describe modules that are only partially present in source.

## ATS Screening Result Explanation Fields

`tbl_screening_results` persists ATS scores and the optional explainable AI layer.
The explanation fields are nullable so older rows remain valid:

```text
strengths
weaknesses
recommendation
confidence
explanation_status
explanation_reason
```

`strengths` and `weaknesses` are JSON arrays stored as `TEXT`. The existing
`ai_summary` column stores the recruiter-readable explanation summary.
