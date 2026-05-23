package org.example.workhub.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.example.workhub.constant.AssignmentStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecruiterTestResultResponse {

    private Long assignmentId;
    private String candidateId;
    private String candidateName;
    private String candidateEmail;
    private Long applicationId;
    private AssignmentStatus status;
    private Double totalScore;
    private Double maxScore;
    private LocalDateTime submittedAt;
}
