package org.example.workhub.domain.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AssessmentTestCreateRequest {

    @NotBlank(message = "{invalid.general.required}")
    private String title;

    private String description;

    @NotNull(message = "{invalid.general.required}")
    @Positive(message = "{invalid.general}")
    private Integer durationMinutes;

    @NotNull(message = "{invalid.general.required}")
    @FutureOrPresent(message = "{invalid.date-future}")
    private LocalDateTime startAt;

    @NotNull(message = "{invalid.general.required}")
    @FutureOrPresent(message = "{invalid.date-future}")
    private LocalDateTime endAt;
}
