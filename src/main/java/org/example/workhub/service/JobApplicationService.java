package org.example.workhub.service;

import org.example.workhub.domain.dto.request.ApplicationStatusRequest;
import org.example.workhub.domain.dto.request.JobApplicationRequest;
import org.example.workhub.domain.dto.pagination.PaginationResponseDto;
import org.example.workhub.domain.dto.response.JobApplicationResponse;

public interface JobApplicationService {

    // ========== Candidate Actions ==========
    JobApplicationResponse applyJob(Long jobId, JobApplicationRequest request);

    void withdrawApplication(Long jobId);

    PaginationResponseDto<JobApplicationResponse> getMyApplications(int page, int size);

    // ========== Recruiter Actions ==========
    PaginationResponseDto<JobApplicationResponse> getJobApplicants(Long jobId, int page, int size);

    PaginationResponseDto<JobApplicationResponse> getCompanyApplications(Long companyId, int page, int size);

    JobApplicationResponse updateApplicationStatus(Long applicationId, ApplicationStatusRequest request);

    // ========== Internal ==========
    boolean hasApplied(Long jobId, String userId);
}