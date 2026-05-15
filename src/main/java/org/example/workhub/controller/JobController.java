package org.example.workhub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.example.workhub.base.RestApiV1;
import org.example.workhub.base.VsResponseUtil;
import org.example.workhub.constant.UrlConstant;
import org.example.workhub.domain.dto.pagination.PaginationResponseDto;
import org.example.workhub.domain.dto.request.JobCreateRequest;
import org.example.workhub.domain.dto.request.JobFilterRequest;
import org.example.workhub.domain.dto.request.JobUpdateRequest;
import org.example.workhub.domain.dto.response.JobResponse;
import org.example.workhub.domain.dto.response.JobStatisticsResponse;
import org.example.workhub.service.JobService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Log4j2
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
@RestApiV1
@Tag(name = "Job Controller", description = "APIs for job management")
public class JobController {

    JobService jobService;

    // ========== CRUD APIs ==========

    @Operation(summary = "Create a new job", description = "Create a new job (Recruiter only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Job created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping(UrlConstant.Job.JOB_BASE)
    @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    public ResponseEntity<?> createJob(@RequestBody @Valid JobCreateRequest request) {
        return VsResponseUtil.success(HttpStatus.CREATED, jobService.createJob(request));
    }

    @Operation(summary = "Update a job", description = "Update job details (Owner or Admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Job updated successfully"),
            @ApiResponse(responseCode = "404", description = "Job not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping(UrlConstant.Job.ID)
    @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateJob(
            @PathVariable @Parameter(description = "Job ID") Long id,
            @RequestBody @Valid JobUpdateRequest request) {
        return VsResponseUtil.success(jobService.updateJob(id, request));
    }

    @Operation(summary = "Get job by ID", description = "Get job details by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Job found"),
            @ApiResponse(responseCode = "404", description = "Job not found")
    })
    @GetMapping(UrlConstant.Job.ID)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getJobById(
            @PathVariable @Parameter(description = "Job ID") Long id) {
        return VsResponseUtil.success(jobService.getJobById(id));
    }

    @Operation(summary = "Get all jobs", description = "Get all jobs with pagination and filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Jobs retrieved successfully")
    })
    @GetMapping(UrlConstant.Job.JOB_BASE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAllJobs(JobFilterRequest filter) {
        return VsResponseUtil.success(jobService.getAllJobs(filter));
    }

    @Operation(summary = "Delete a job", description = "Soft delete a job (Owner or Admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Job deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Job not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @DeleteMapping(UrlConstant.Job.ID)
    @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteJob(
            @PathVariable @Parameter(description = "Job ID") Long id) {
        jobService.deleteJob(id);
        return VsResponseUtil.success("Job deleted successfully");
    }

    // ========== Publish/Unpublish APIs ==========

    @Operation(summary = "Publish a job", description = "Publish a job (Owner or Admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Job published successfully"),
            @ApiResponse(responseCode = "404", description = "Job not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping(UrlConstant.Job.PUBLISH)
    @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    public ResponseEntity<?> publishJob(
            @PathVariable @Parameter(description = "Job ID") Long id) {
        return VsResponseUtil.success(jobService.publishJob(id));
    }

    @Operation(summary = "Unpublish a job", description = "Unpublish a job (Owner or Admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Job unpublished successfully"),
            @ApiResponse(responseCode = "404", description = "Job not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping(UrlConstant.Job.UNPUBLISH)
    @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    public ResponseEntity<?> unpublishJob(
            @PathVariable @Parameter(description = "Job ID") Long id) {
        return VsResponseUtil.success(jobService.unpublishJob(id));
    }

    // ========== Statistics ==========

    @Operation(summary = "Get job statistics", description = "Get job statistics (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping(UrlConstant.Job.STATISTICS)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getJobStatistics() {
        return VsResponseUtil.success(jobService.getJobStatistics());
    }
}