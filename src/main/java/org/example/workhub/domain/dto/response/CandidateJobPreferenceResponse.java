package org.example.workhub.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.workhub.constant.CandidateLevel;
import org.example.workhub.constant.EmploymentType;
import org.example.workhub.constant.WorkMode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CandidateJobPreferenceResponse {

    private Long id;

    private String candidateId;

    private String desiredJobTitle;

    private String preferredLocation;

    private WorkMode workMode;

    private EmploymentType employmentType;

    private CandidateLevel candidateLevel;

    private Integer experienceYears;

    private BigDecimal expectedSalaryMin;

    private BigDecimal expectedSalaryMax;

    private List<SkillInfo> skills;

    private LocalDateTime createdDate;

    private LocalDateTime lastModifiedDate;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SkillInfo {
        private Long id;
        private String name;
        private String slug;
    }
}
