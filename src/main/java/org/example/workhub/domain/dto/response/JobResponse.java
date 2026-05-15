package org.example.workhub.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.example.workhub.constant.LevelEnum;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Job response DTO")
public class JobResponse {

    @Schema(description = "Job ID", example = "1")
    private Long id;

    @Schema(description = "Job title", example = "Senior Java Developer")
    private String title;

    @Schema(description = "Job slug", example = "senior-java-developer")
    private String slug;

    @Schema(description = "Job description")
    private String description;

    @Schema(description = "Job requirements")
    private String requirement;

    @Schema(description = "Job benefits")
    private String benefit;

    @Schema(description = "Job location", example = "Ho Chi Minh City")
    private String location;

    @Schema(description = "Minimum salary", example = "1000")
    private String salaryMin;

    @Schema(description = "Maximum salary", example = "3000")
    private String salaryMax;

    @Schema(description = "Salary is negotiable", example = "true")
    private Boolean negotiableSalary;

    @Schema(description = "Job level", example = "MIDDLE")
    private LevelEnum level;

    @Schema(description = "Employment type", example = "FULL_TIME")
    private String employmentType;

    @Schema(description = "Required experience years", example = "3")
    private Integer experienceYears;

    @Schema(description = "Number of positions", example = "2")
    private Integer quantity;

    @Schema(description = "Job expiry date")
    private LocalDate expiredAt;

    @Schema(description = "Start date")
    private LocalDate startDate;

    @Schema(description = "Is published", example = "true")
    private Boolean published;

    @Schema(description = "Is expired", example = "false")
    private Boolean expired;

    @Schema(description = "Is deleted", example = "false")
    private Boolean deleted;

    @Schema(description = "Company info")
    private CompanyBasicInfo company;

    @Schema(description = "Recruiter info")
    private RecruiterInfo recruiter;

    @Schema(description = "List of skills")
    private List<SkillInfo> skills;

    @Schema(description = "Number of applications")
    private Integer applicationCount;

    @Schema(description = "Created date")
    private LocalDateTime createdDate;

    @Schema(description = "Last modified date")
    private LocalDateTime lastModifiedDate;

    @Schema(description = "Created by")
    private String createdBy;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CompanyBasicInfo {
        private Long id;
        private String name;
        private String logo;
        private String address;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RecruiterInfo {
        private String id;
        private String username;
        private String email;
        private String avatar;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SkillInfo {
        private Long id;
        private String name;
        private String level;
    }
}