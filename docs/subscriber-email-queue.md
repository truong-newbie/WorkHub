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
  -> render HTML email template
  -> create tbl_email_queues rows with status PENDING and isHtml=true
  -> EmailQueueWorker picks pending rows
  -> EmailService sends real SMTP HTML email
  -> queue status becomes SENT or FAILED
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

## HTML Template

The email template receives these variables:

- `subscriberName`
- `header`
- `footer`
- `generatedAt`
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

`Subscriber.lastEmailSentAt` is updated only after the queue item is sent successfully. This avoids marking jobs as delivered when SMTP failed.
