package org.example.workhub.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.workhub.constant.WorkMode;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecommendedJobResponse {

    private Long jobId;

    private Long id;

    private String title;

    private String slug;

    private Long companyId;

    private String companyName;

    private String companyLogo;

    private String location;

    private String salaryMin;

    private String salaryMax;

    private Boolean negotiableSalary;

    private String level;

    private Integer experienceYears;

    private WorkMode workMode;

    private String employmentType;

    private Double matchScore;

    private Double contentScore;

    private Double behaviorScore;

    private Double collaborativeScore;

    private Double hybridScore;

    private List<String> skillNames;

    private Instant expiredAt;

    private List<String> matchedSkills;

    private List<String> missingSkills;

    private List<String> matchReasons;

    private List<String> reasonCodes;

    private List<RecommendationReasonResponse> reasons;

    private String reasonText;

    private LocalDateTime createdDate;
}
