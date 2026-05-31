package org.example.workhub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.workhub.base.RestApiV1;
import org.example.workhub.base.VsResponseUtil;
import org.example.workhub.constant.RecruiterRequestStatus;
import org.example.workhub.constant.UrlConstant;
import org.example.workhub.domain.dto.request.RecruiterRequestCreateRequest;
import org.example.workhub.domain.dto.request.RecruiterRequestReviewRequest;
import org.example.workhub.service.RecruiterRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@RestApiV1
@RequiredArgsConstructor
public class RecruiterRequestController {

    private final RecruiterRequestService recruiterRequestService;

    @Operation(summary = "Candidate requests to become a recruiter", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(UrlConstant.RecruiterRequest.BASE)
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<?> create(
            @RequestBody(required = false) @Valid RecruiterRequestCreateRequest request) {
        return VsResponseUtil.success(HttpStatus.CREATED, recruiterRequestService.create(request));
    }

    @Operation(summary = "Get current user recruiter requests", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(UrlConstant.RecruiterRequest.ME)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return VsResponseUtil.success(recruiterRequestService.getMyRequests(page, size));
    }

    @Operation(summary = "Admin gets recruiter requests", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(UrlConstant.RecruiterRequest.BASE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getRequests(
            @RequestParam(required = false) RecruiterRequestStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return VsResponseUtil.success(recruiterRequestService.getRequests(status, page, size));
    }

    @Operation(summary = "Admin approves recruiter request", security = @SecurityRequirement(name = "bearerAuth"))
    @PatchMapping(UrlConstant.RecruiterRequest.APPROVE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approve(
            @PathVariable Long requestId,
            @RequestBody(required = false) @Valid RecruiterRequestReviewRequest request) {
        return VsResponseUtil.success(recruiterRequestService.approve(requestId, request));
    }

    @Operation(summary = "Admin rejects recruiter request", security = @SecurityRequirement(name = "bearerAuth"))
    @PatchMapping(UrlConstant.RecruiterRequest.REJECT)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> reject(
            @PathVariable Long requestId,
            @RequestBody(required = false) @Valid RecruiterRequestReviewRequest request) {
        return VsResponseUtil.success(recruiterRequestService.reject(requestId, request));
    }
}
