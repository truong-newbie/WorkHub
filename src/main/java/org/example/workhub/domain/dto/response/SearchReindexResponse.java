package org.example.workhub.domain.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SearchReindexResponse {

    private long indexedCount;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    private long durationMs;
}
