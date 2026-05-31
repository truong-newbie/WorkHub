# Notification Module

## Purpose

Handles stored notifications and realtime WebSocket push to authenticated users.

## Main Files

- `controller/NotificationController.java`
- `service/NotificationService.java`
- `service/impl/NotificationServiceImpl.java`
- `domain.entity.Notification.java`
- `domain.mapper.NotificationMapper.java`
- `domain.specification.NotificationSpecification.java`
- `listener/NotificationEventListener.java`
- `config/WebSocketConfig.java`
- `security/websocket/*`

## Key APIs

- `GET /api/v1/notifications`
- `GET /api/v1/notifications/unread-count`
- `PUT /api/v1/notifications/{id}/read`
- `PUT /api/v1/notifications/read-all`
- `DELETE /api/v1/notifications/{id}`

## WebSocket

Endpoint:

```text
/ws
```

User destination:

```text
/user/queue/notifications
```

## Event Coverage

Existing events include job application created/status updated, assessment assigned/submitted, company moderation, and ATS-related notification handoff.

## Queue Integration

`NotificationQueueConsumer` reuses `NotificationService.createAndSend(...)`.

## AI Notes

Do not create a duplicate notification service. Use `NotificationService`.
