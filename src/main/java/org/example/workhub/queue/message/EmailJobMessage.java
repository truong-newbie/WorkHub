package org.example.workhub.queue.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailJobMessage {

    private String eventId;
    private String to;
    private String subject;
    private String content;
    private String templateCode;
    private Long subscriberId;
    private String userId;
    private List<Long> jobIds;
    private LocalDateTime createdAt;
}
