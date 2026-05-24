package org.example.workhub.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NotificationCountResponse {

    private long unreadCount;
}
