package org.example.workhub.domain.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.example.workhub.constant.QuestionType;

import java.util.List;

@Getter
@Setter
public class AssessmentQuestionCreateRequest {

    @NotNull(message = "{invalid.general.required}")
    private QuestionType type;

    @NotBlank(message = "{invalid.general.required}")
    private String content;

    @NotNull(message = "{invalid.general.required}")
    @Positive(message = "{invalid.general}")
    private Double score;

    private Integer orderIndex;

    @Valid
    private List<AssessmentOptionRequest> options;
}
