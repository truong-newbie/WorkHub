package org.example.workhub.domain.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubmitAnswerRequest {

    @NotNull(message = "{invalid.general.required}")
    private Long questionId;

    private Long selectedOptionId;

    @Size(max = 10000, message = "{invalid.general}")
    private String essayAnswer;
}
