package org.example.workhub.domain.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScoreEssayAnswerRequest {

    @NotNull(message = "{invalid.general.required}")
    @PositiveOrZero(message = "{invalid.general}")
    private Double score;

    private String feedback;
}
