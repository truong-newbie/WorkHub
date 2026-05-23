package org.example.workhub.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CandidateAnswerResponse {

    private Long id;
    private Long questionId;
    private String questionContent;
    private Long selectedOptionId;
    private String selectedOptionContent;
    private String essayAnswer;
    private Double score;
    private Double maxScore;
    private String feedback;
}
