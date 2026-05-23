package org.example.workhub.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.example.workhub.constant.AssignmentStatus;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CandidateTestResultResponse {

    private Long assignmentId;
    private Long testId;
    private String testTitle;
    private AssignmentStatus status;
    private Double totalScore;
    private Double maxScore;
    private LocalDateTime submittedAt;
    private String recruiterFeedback;
    private List<CandidateAnswerResponse> answers;
}
