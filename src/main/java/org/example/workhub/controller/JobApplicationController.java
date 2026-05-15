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
import org.example.workhub.domain.dto.request.ApplicationStatusRequest;
import org.example.workhub.domain.dto.request.JobApplicationRequest;
import org.example.workhub.domain.dto.response.JobApplicationResponse;
import org.example.workhub.service.JobApplicationService;
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
@Tag(name = "Job Application Controller", description = "APIs for job application management")
public class JobApplicationController {

    JobApplicationService applicationService;

    // ========== Candidate APIs ==========

    @Operation(summary = "Apply for a job", description = "Apply for a job (Candidate)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Application submitted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or already applied"),
            @ApiResponse(responseCode = "404", description = "Job not found")
    })
    @PostMapping(UrlConstant.JobApplication.APPLY)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> applyJob(
            @PathVariable @Parameter(description = "Job ID") Long jobId,
            @RequestBody @Valid JobApplicationRequest request) {
        return VsResponseUtil.success(HttpStatus.CREATED, applicationService.applyJob(jobId, request));
    }

    @Operation(summary = "Withdraw application", description = "Withdraw job application (Candidate)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application withdrawn successfully"),
            @ApiResponse(responseCode = "400", description = "Cannot withdraw"),
            @ApiResponse(responseCode = "404", description = "Application not found")
    })
    @DeleteMapping(UrlConstant.JobApplication.WITHDRAW)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> withdrawApplication(
            @PathVariable @Parameter(description = "Job ID") Long jobId) {
        applicationService.withdrawApplication(jobId);
        return VsResponseUtil.success("Application withdrawn successfully");
    }

    @Operation(summary = "Get my applications", description = "Get current user's job applications")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Applications retrieved successfully")
    })
    @GetMapping(UrlConstant.JobApplication.MY_APPLICATIONS)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return VsResponseUtil.success(applicationService.getMyApplications(page, size));
    }

    // ========== Recruiter APIs ==========

    @Operation(summary = "Get job applicants", description = "View applicants for a job (Recruiter)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Applicants retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Job not found")
    })
    @GetMapping(UrlConstant.JobApplication.JOB_APPLICATIONS)
    @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    public ResponseEntity<?> getJobApplicants(
            @PathVariable @Parameter(description = "Job ID") Long jobId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return VsResponseUtil.success(applicationService.getJobApplicants(jobId, page, size));
    }

    @Operation(summary = "Update application status", description = "Approve or reject application (Recruiter)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Application not found")
    })
    @PutMapping(UrlConstant.JobApplication.UPDATE_STATUS)
    @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateApplicationStatus(
            @PathVariable @Parameter(description = "Application ID") Long applicationId,
            @RequestBody @Valid ApplicationStatusRequest request) {
        return VsResponseUtil.success(applicationService.updateApplicationStatus(applicationId, request));
    }
}