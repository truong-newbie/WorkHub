package org.example.workhub.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.example.workhub.constant.AssessmentStatus;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssessmentTestResponse {

    private Long id;
    private String title;
    private String description;
    private Integer durationMinutes;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private AssessmentStatus status;
    private Long jobId;
    private String jobTitle;
    private String recruiterId;
    private String recruiterName;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private List<AssessmentQuestionResponse> questions;
}
