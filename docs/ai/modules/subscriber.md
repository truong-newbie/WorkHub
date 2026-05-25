# Subscriber Module

## Purpose

Handles candidate job email subscriptions by skills, subscriber management, unsubscribe token, scheduled matching emails, and manual subscriber mail trigger.

## Main Files

- `controller/SubscriberController.java`
- `service/SubscriberService.java`
- `service/SubscriberMailScheduler.java`
- `service/EmailQueueService.java`
- `service/impl/SubscriberServiceImpl.java`
- `service/impl/EmailQueueServiceImpl.java`
- `domain.entity.Subscriber.java`
- `domain.entity.EmailQueue.java`
- `domain.entity.SubscriberJobNotification.java`
- `repository/SubscriberRepository.java`
- `repository/EmailQueueRepository.java`

## Key APIs

- `/api/v1/subscribers`
- `/api/v1/subscribers/{id}`
- `/api/v1/subscribers/me`
- `/api/v1/subscribers/{id}/enable`
- `/api/v1/subscribers/{id}/disable`
- `POST /api/v1/subscribers/mail/send`
- `POST /api/v1/subscribers/mail/queue/process`
- `GET /api/v1/subscribers/unsubscribe?token=...`

## Matching Rules

Matching jobs must be:

- published
- non-deleted
- not expired
- sharing at least one subscriber skill
- not already sent or pending for that subscriber/job

## Queue Integration

Subscriber matching creates DB `EmailQueue` and `SubscriberJobNotification` rows, then publishes `EmailJobMessage` to RabbitMQ after commit.

`EmailQueueConsumer` sends through existing `EmailService`, marks queue/notifications sent, and updates `Subscriber.lastEmailSentAt`.

## AI Notes

Do not create another mail service or subscriber scheduler. Reuse existing email/subscriber classes.
