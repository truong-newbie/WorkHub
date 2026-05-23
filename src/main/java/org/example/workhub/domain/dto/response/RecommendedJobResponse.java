package org.example.workhub.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.workhub.constant.WorkMode;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecommendedJobResponse {

    private Long jobId;

    private String title;

    private String companyName;

    private String location;

    private String salaryMin;

    private String salaryMax;

    private Integer experienceYears;

    private WorkMode workMode;

    private String employmentType;

    private Double matchScore;

    private List<String> matchedSkills;

    private List<String> missingSkills;

    private List<String> matchReasons;

    private LocalDateTime createdDate;
}
