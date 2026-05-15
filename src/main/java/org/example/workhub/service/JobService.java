package org.example.workhub.service;

import org.example.workhub.domain.dto.request.JobCreateRequest;
import org.example.workhub.domain.dto.request.JobFilterRequest;
import org.example.workhub.domain.dto.request.JobUpdateRequest;
import org.example.workhub.domain.dto.pagination.PaginationResponseDto;
import org.example.workhub.domain.dto.response.JobResponse;
import org.example.workhub.domain.dto.response.JobStatisticsResponse;
import org.example.workhub.domain.entity.Job;

public interface JobService {

    // ========== CRUD ==========
    JobResponse createJob(JobCreateRequest request);

    JobResponse updateJob(Long id, JobUpdateRequest request);

    JobResponse getJobById(Long id);

    PaginationResponseDto<JobResponse> getAllJobs(JobFilterRequest filter);

    void deleteJob(Long id);

    // ========== Publish/Unpublish ==========
    JobResponse publishJob(Long id);

    JobResponse unpublishJob(Long id);

    // ========== Statistics ==========
    JobStatisticsResponse getJobStatistics();

    // ========== Internal ==========
    Job findById(Long id);
}