package org.example.workhub.queue.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtsScreeningJobMessage {

    private String eventId;
    private Long applicationId;
    private Long resumeId;
    private Long jobId;
    private String requestedByUserId;
    private LocalDateTime createdAt;
}
