package org.example.workhub.domain.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.example.workhub.constant.QuestionType;

import java.util.List;

@Getter
@Setter
public class AssessmentQuestionUpdateRequest {

    private QuestionType type;

    private String content;

    @Positive(message = "{invalid.general}")
    private Double score;

    private Integer orderIndex;

    @Valid
    private List<AssessmentOptionRequest> options;
}
