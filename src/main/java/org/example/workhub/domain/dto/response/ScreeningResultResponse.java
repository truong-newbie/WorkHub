package org.example.workhub.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScreeningResultResponse {
    private Long id;
    private Long applicationId;
    private String candidateId;
    private String candidateName;
    private Long jobId;
    private String jobTitle;
    private Double totalScore;
    private Double skillScore;
    private Double semanticScore;
    private Double experienceScore;
    private Double educationScore;
    private List<String> matchedSkills;
    private List<String> missingSkills;
    private List<String> extraSkills;
    private String aiSummary;
    private LocalDateTime screenedAt;
}
