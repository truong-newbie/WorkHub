package org.example.workhub.domain.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.example.workhub.constant.NotificationType;
import org.example.workhub.domain.dto.pagination.PaginationFullRequestDto;

import java.time.LocalDateTime;

@Getter
@Setter
public class NotificationSearchRequest extends PaginationFullRequestDto {

    private NotificationType type;

    private Boolean read;

    private LocalDateTime fromDate;

    private LocalDateTime toDate;
}
