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
import org.example.workhub.domain.dto.request.ResumeSearchRequest;
import org.example.workhub.domain.dto.request.ResumeUpdateRequest;
import org.example.workhub.domain.dto.request.ResumeUploadRequest;
import org.example.workhub.service.ResumeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Log4j2
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
@RestApiV1
@Tag(name = "Resume Controller", description = "APIs for candidate resume management")
public class ResumeController {

    ResumeService resumeService;

    @Operation(summary = "Upload resume", description = "Candidate uploads a PDF, DOC or DOCX resume")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Resume uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file or request"),
            @ApiResponse(responseCode = "409", description = "Duplicate resume title")
    })
    @PostMapping(value = UrlConstant.Resume.RESUME_BASE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('CANDIDATE') or hasRole('ADMIN')")
    public ResponseEntity<?> uploadResume(@ModelAttribute @Valid ResumeUploadRequest request) {
        return VsResponseUtil.success(HttpStatus.CREATED, resumeService.uploadResume(request));
    }

    @Operation(summary = "Update resume metadata", description = "Update title, visibility, default flag, skills and ATS metadata")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resume updated successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Resume not found")
    })
    @PutMapping(UrlConstant.Resume.ID)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateResume(
            @PathVariable @Parameter(description = "Resume ID") Long id,
            @RequestBody @Valid ResumeUpdateRequest request) {
        return VsResponseUtil.success(resumeService.updateResume(id, request));
    }

    @Operation(summary = "Replace resume file", description = "Replace an existing resume file")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resume file updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file"),
            @ApiResponse(responseCode = "404", description = "Resume not found")
    })
    @PutMapping(value = UrlConstant.Resume.FILE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateResumeFile(
            @PathVariable @Parameter(description = "Resume ID") Long id,
            @RequestPart("file") MultipartFile file) {
        return VsResponseUtil.success(resumeService.updateResumeFile(id, file));
    }

    @Operation(summary = "Delete resume", description = "Soft delete a resume")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resume deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Resume not found")
    })
    @DeleteMapping(UrlConstant.Resume.ID)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteResume(@PathVariable @Parameter(description = "Resume ID") Long id) {
        return VsResponseUtil.success(resumeService.deleteResume(id));
    }

    @Operation(summary = "Get resume detail", description = "Get resume detail by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resume found"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Resume not found")
    })
    @GetMapping(UrlConstant.Resume.ID)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getResumeDetail(@PathVariable @Parameter(description = "Resume ID") Long id) {
        return VsResponseUtil.success(resumeService.getResumeDetail(id));
    }

    @Operation(summary = "Get my resumes", description = "Get current user's resumes with search, filter and pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resumes retrieved successfully")
    })
    @GetMapping(UrlConstant.Resume.MY_RESUMES)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyResumes(@ModelAttribute ResumeSearchRequest request) {
        return VsResponseUtil.success(resumeService.getMyResumes(request));
    }

    @Operation(summary = "Admin search resumes", description = "Admin searches all resumes with filters and pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resumes retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping(UrlConstant.Resume.ADMIN_RESUMES)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllResumes(@ModelAttribute ResumeSearchRequest request) {
        return VsResponseUtil.success(resumeService.getAllResumes(request));
    }

    @Operation(summary = "Set default resume", description = "Set one resume as current user's default resume")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Default resume updated successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Resume not found")
    })
    @PutMapping(UrlConstant.Resume.DEFAULT)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> setDefaultResume(@PathVariable @Parameter(description = "Resume ID") Long id) {
        return VsResponseUtil.success(resumeService.setDefaultResume(id));
    }

    @Operation(summary = "Download my resume", description = "Return file URL and metadata for resume download")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resume download metadata returned"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Resume not found")
    })
    @GetMapping(UrlConstant.Resume.DOWNLOAD)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> downloadMyResume(@PathVariable @Parameter(description = "Resume ID") Long id) {
        return VsResponseUtil.success(resumeService.downloadMyResume(id));
    }

    @Operation(summary = "Recruiter view candidate resume", description = "Recruiter views a candidate resume after the candidate applied to a job")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resume returned"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Resume not found")
    })
    @GetMapping(UrlConstant.Resume.RECRUITER_CANDIDATE_RESUME)
    @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    public ResponseEntity<?> getCandidateResumeForJob(
            @PathVariable @Parameter(description = "Job ID") Long jobId,
            @PathVariable @Parameter(description = "Candidate ID") String candidateId) {
        return VsResponseUtil.success(resumeService.getCandidateResumeForJob(jobId, candidateId));
    }

    @Operation(summary = "Recruiter download candidate resume", description = "Recruiter gets candidate resume download metadata after application")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resume download metadata returned"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Resume not found")
    })
    @GetMapping(UrlConstant.Resume.RECRUITER_DOWNLOAD)
    @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    public ResponseEntity<?> downloadCandidateResumeForJob(
            @PathVariable @Parameter(description = "Job ID") Long jobId,
            @PathVariable @Parameter(description = "Candidate ID") String candidateId) {
        return VsResponseUtil.success(resumeService.downloadCandidateResumeForJob(jobId, candidateId));
    }
}
