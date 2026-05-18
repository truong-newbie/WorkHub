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
public class CandidateTestAssignmentResponse {

    private Long id;
    private Long testId;
    private String testTitle;
    private String testDescription;
    private Long applicationId;
    private String candidateId;
    private String candidateName;
    private String candidateEmail;
    private AssignmentStatus status;
    private Integer durationMinutes;
    private LocalDateTime testStartAt;
    private LocalDateTime testEndAt;
    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
    private Double totalScore;
    private Double maxScore;
    private String recruiterFeedback;
}
