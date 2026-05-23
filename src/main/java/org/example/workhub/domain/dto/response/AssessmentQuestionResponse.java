package org.example.workhub.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.example.workhub.constant.QuestionType;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssessmentQuestionResponse {

    private Long id;
    private String content;
    private QuestionType type;
    private Double score;
    private Integer orderIndex;
    private List<AssessmentOptionResponse> options;
}
