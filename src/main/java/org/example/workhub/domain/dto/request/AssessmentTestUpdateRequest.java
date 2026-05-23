package org.example.workhub.domain.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AssessmentTestUpdateRequest {

    private String title;

    private String description;

    @Positive(message = "{invalid.general}")
    private Integer durationMinutes;

    @FutureOrPresent(message = "{invalid.date-future}")
    private LocalDateTime startAt;

    @FutureOrPresent(message = "{invalid.date-future}")
    private LocalDateTime endAt;
}
