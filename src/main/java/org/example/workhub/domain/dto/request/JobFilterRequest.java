package org.example.workhub.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to filter and search jobs")
public class JobFilterRequest {

    @Schema(description = "Search keyword in title", example = "java")
    private String keyword;

    @Schema(description = "Company ID", example = "1")
    private Long companyId;

    @Schema(description = "Minimum salary", example = "1000")
    private String salaryMin;

    @Schema(description = "Maximum salary", example = "5000")
    private String salaryMax;

    @Schema(description = "Location", example = "Ho Chi Minh")
    private String location;

    @Schema(description = "Job level", example = "MIDDLE")
    private String level;

    @Schema(description = "Employment type", example = "FULL_TIME")
    private String employmentType;

    @Schema(description = "Skill IDs", example = "[1, 2, 3]")
    private String skills;

    @Schema(description = "Minimum experience years", example = "2")
    private Integer experienceYearsMin;

    @Schema(description = "Maximum experience years", example = "5")
    private Integer experienceYearsMax;

    @Schema(description = "Only published jobs", example = "true")
    private Boolean published = true;

    @Schema(description = "Include expired jobs", example = "false")
    private Boolean includeExpired = false;

    @Schema(description = "Page number (0-based)", example = "0")
    private Integer page = 0;

    @Schema(description = "Page size", example = "10")
    private Integer size = 10;

    @Schema(description = "Sort field", example = "createdDate")
    private String sortBy = "createdDate";

    @Schema(description = "Sort direction (ASC/DESC)", example = "DESC")
    private String sortDir = "DESC";
}