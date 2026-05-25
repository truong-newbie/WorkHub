# Queue System Module

## Purpose

RabbitMQ-backed background job system for WorkHub.

## Main Files

- `queue/config/RabbitMQConfig.java`
- `queue/config/QueueRoutingKeys.java`
- `queue/message/*`
- `queue/producer/BackgroundJobProducer.java`
- `queue/consumer/*`
- `service/impl/EmailQueueServiceImpl.java`
- `domain.entity.EmailQueue.java`

## Exchange

```text
workhub.exchange
```

## Queues

- `email.queue`
- `ats.screening.queue`
- `resume.parsing.queue`
- `notification.queue`
- `email.dlq`
- `ats.screening.dlq`
- `resume.parsing.dlq`
- `notification.dlq`

## Routing Keys

- `email.send`
- `ats.screening.request`
- `resume.parsing.request`
- `notification.send`

DLQ routing keys:

- `email.failed`
- `ats.screening.failed`
- `resume.parsing.failed`
- `notification.failed`

## Message Rule

Messages contain IDs and minimal payload only. Consumers load entities from DB.

## Retry/DLQ

Listener retry is configured through:

```properties
workhub.queue.retry.max-attempts=3
```

After retries are exhausted, rejected messages go to DLQ.

## Email Flow

Subscriber matching reuses DB `EmailQueue` as tracking/idempotency and RabbitMQ as transport. Email publishing happens after DB commit.

## Test Docs

- `docs/background-job-queue-system.md`
- `docs/background-job-queue-test-checklist.md`
- `postman/WorkHub_Background_Queue_APIs.postman_collection.json`
