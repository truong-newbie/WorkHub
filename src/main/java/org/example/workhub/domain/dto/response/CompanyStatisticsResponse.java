package org.example.workhub.domain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Company statistics response")
public class CompanyStatisticsResponse {

    private Long companyId;
    private Long totalJobs;
    private Long activeJobs;
    private Long inactiveJobs;
    private Long totalApplications;
    private Long pendingApplications;
    private Long acceptedApplications;
    private Long rejectedApplications;
}
