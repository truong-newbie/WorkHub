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
public class ResumeParsingJobMessage {

    private String eventId;
    private Long resumeId;
    private String fileUrl;
    private String userId;
    private LocalDateTime createdAt;
}
