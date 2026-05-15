package org.example.workhub.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Job statistics response")
public class JobStatisticsResponse {

    @Schema(description = "Total jobs", example = "150")
    private Long totalJobs;

    @Schema(description = "Published jobs", example = "120")
    private Long publishedJobs;

    @Schema(description = "Draft jobs", example = "30")
    private Long draftJobs;

    @Schema(description = "Expired jobs", example = "45")
    private Long expiredJobs;

    @Schema(description = "Total applications", example = "500")
    private Long totalApplications;

    @Schema(description = "Pending applications", example = "100")
    private Long pendingApplications;

    @Schema(description = "Approved applications", example = "50")
    private Long approvedApplications;

    @Schema(description = "Rejected applications", example = "40")
    private Long rejectedApplications;

    @Schema(description = "Jobs by level")
    private Map<String, Long> jobsByLevel;

    @Schema(description = "Jobs by location")
    private Map<String, Long> jobsByLocation;

    @Schema(description = "New jobs this month", example = "25")
    private Long newJobsThisMonth;

    @Schema(description = "New jobs today", example = "3")
    private Long newJobsToday;
}