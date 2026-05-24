# WorkHub Notification WebSocket Guide

## WebSocket URL

```text
ws://localhost:8080/ws?token=<access_token>
```

SockJS clients should use:

```text
http://localhost:8080/ws?token=<access_token>
```

## Subscribe

```text
/user/queue/notifications
```

## REST APIs

- `GET /api/v1/notifications`
- `GET /api/v1/notifications/unread-count`
- `PUT /api/v1/notifications/{id}/read`
- `PUT /api/v1/notifications/read-all`
- `DELETE /api/v1/notifications/{id}`

## Example JS

```javascript
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";

const accessToken = "<access_token>";

const client = new Client({
  webSocketFactory: () => new SockJS(`http://localhost:8080/ws?token=${accessToken}`),
  connectHeaders: {
    Authorization: `Bearer ${accessToken}`,
  },
  reconnectDelay: 5000,
  onConnect: () => {
    client.subscribe("/user/queue/notifications", (message) => {
      const notification = JSON.parse(message.body);
      console.log("New notification", notification);
    });
  },
});

client.activate();
```
