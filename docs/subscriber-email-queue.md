# Subscriber Email Queue Async

## Goal

Subscriber matching no longer sends SMTP mail directly. It now creates email queue records and a background worker sends those emails asynchronously.

Queued subscriber emails are rendered from the Thymeleaf HTML template:

```text
src/main/resources/templates/email/subscriber-job-matching.html
```

## Flow

```text
Subscriber matching scheduler or manual API
  -> find subscribers with matching jobs
  -> exclude jobs already SENT or PENDING for that subscriber
  -> render HTML email template
  -> create tbl_email_queues rows with status PENDING and isHtml=true
  -> create tbl_subscriber_job_notifications rows with status PENDING for each job in the email
  -> EmailQueueWorker picks pending rows
  -> EmailService sends real SMTP HTML email
  -> queue status becomes SENT or FAILED
  -> per-job notification status becomes SENT or FAILED
```

## Manual APIs

### Queue matching emails

```http
POST http://localhost:8080/api/v1/subscribers/mail/send
Authorization: Bearer <admin_or_recruiter_token>
```

Expected response:

```json
{
  "status": "SUCCESS",
  "data": {
    "checkedSubscribers": 10,
    "sentEmails": 0,
    "queuedEmails": 3,
    "matchedJobs": 8
  }
}
```

### Process pending email queue immediately

```http
POST http://localhost:8080/api/v1/subscribers/mail/queue/process
Authorization: Bearer <admin_or_recruiter_token>
```

Expected response:

```json
{
  "status": "SUCCESS",
  "data": {
    "checkedEmails": 20,
    "sentEmails": 18,
    "retriedEmails": 1,
    "failedEmails": 1
  }
}
```

## Automatic Jobs

Matching job discovery:

```properties
subscriber.mail.cron=0 0 */6 * * *
```

Default: every 6 hours, subscribers are checked and matching emails are queued.

Queue worker:

```properties
email.queue.worker.initial-delay-ms=30000
email.queue.worker.delay-ms=60000
```

Default: waits 30 seconds after app startup, then every 60 seconds pending email queue items are sent.

## Queue Status

- `PENDING`: waiting to be sent.
- `PROCESSING`: worker is sending it.
- `SENT`: SMTP send succeeded.
- `FAILED`: max retry count reached.

## Per-Job Delivery Tracking

Each job included in a subscriber email is tracked in:

```text
tbl_subscriber_job_notifications
```

Tracked fields:

- `subscriber_id`
- `job_id`
- `email_queue_id`
- `email`
- `status`
- `sent_at`
- `failed_at`
- `error_message`

Tracking statuses:

- `PENDING`: the job is already queued for this subscriber.
- `SENT`: the job was successfully sent to this subscriber.
- `FAILED`: the queue reached max retry and this job was not delivered.

Matching excludes jobs with `SENT` or `PENDING` tracking records for that subscriber. This prevents duplicate delivery while still allowing jobs with `FAILED` tracking to be retried later.

`Subscriber.lastEmailSentAt` is now metadata for the last successful email batch. Duplicate prevention is handled by `tbl_subscriber_job_notifications`.

## HTML Template

The email template receives these variables:

- `subscriberName`
- `header`
- `footer`
- `generatedAt`
- `unsubscribeUrl`
- `jobs`

Each job item contains:

- `id`
- `title`
- `companyName`
- `location`
- `salary`
- `employmentType`
- `level`
- `skills`
- `url`

The job URL uses:

```properties
app.frontend-url=http://localhost:3000
```

For example, job `id=1` becomes:

```text
http://localhost:3000/jobs/1
```

## Unsubscribe Token

Each subscriber has a durable `unsubscribeToken`. The HTML email includes a public unsubscribe link:

```http
GET http://localhost:8080/api/v1/subscribers/unsubscribe?token=<unsubscribe_token>
```

No bearer token is required for this endpoint because it is designed to be opened directly from an email.

The backend URL uses:

```properties
app.backend-url=http://localhost:8080/api/v1
```

Expected response:

```json
{
  "status": "SUCCESS",
  "data": {
    "email": "candidate@gmail.com",
    "enabled": false,
    "unsubscribedAt": "2026-05-16T21:30:00",
    "message": "Unsubscribed successfully"
  }
}
```

When the link is opened, the subscriber is not deleted. The system only sets:

```text
enabled = false
unsubscribedAt = now
```

If the user enables subscriber again from the authenticated API, `unsubscribedAt` is cleared.

## Retry Rule

Each email queue item has:

```text
retryCount
maxRetry = 3
nextAttemptAt
errorMessage
```

When SMTP send fails, the worker increases `retryCount` and delays the next attempt by `retryCount * 5` minutes. After 3 failures, the item becomes `FAILED`.

## lastEmailSentAt Behavior

`Subscriber.lastEmailSentAt` is updated only after the queue item is sent successfully. This avoids marking a batch as delivered when SMTP failed.
