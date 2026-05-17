package org.example.workhub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.workhub.base.RestApiV1;
import org.example.workhub.base.VsResponseUtil;
import org.example.workhub.constant.UrlConstant;
import org.example.workhub.domain.dto.request.CompanyJoinRequestCreateRequest;
import org.example.workhub.domain.dto.request.CompanyJoinRequestReviewRequest;
import org.example.workhub.service.CompanyJoinRequestService;
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
public class CompanyJoinRequestController {

    private final CompanyJoinRequestService companyJoinRequestService;

    @Operation(summary = "Recruiter requests to join a company", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(UrlConstant.Company.JOIN_REQUESTS)
//    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<?> requestJoinCompany(
            @PathVariable Long companyId,
            @RequestBody @Valid CompanyJoinRequestCreateRequest request) {
        return VsResponseUtil.success(HttpStatus.CREATED, companyJoinRequestService.requestJoinCompany(companyId, request));
    }

    @Operation(summary = "Get current recruiter company join requests", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(UrlConstant.Company.JOIN_REQUESTS_ME)
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<?> getMyRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return VsResponseUtil.success(companyJoinRequestService.getMyRequests(page, size));
    }

    @Operation(summary = "Get company join requests", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(UrlConstant.Company.JOIN_REQUESTS)
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    public ResponseEntity<?> getCompanyRequests(
            @PathVariable Long companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return VsResponseUtil.success(companyJoinRequestService.getCompanyRequests(companyId, page, size));
    }

    @Operation(summary = "Approve company join request", security = @SecurityRequirement(name = "bearerAuth"))
    @PatchMapping(UrlConstant.Company.APPROVE_JOIN_REQUEST)
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    public ResponseEntity<?> approve(
            @PathVariable Long requestId,
            @RequestBody(required = false) @Valid CompanyJoinRequestReviewRequest request) {
        return VsResponseUtil.success(companyJoinRequestService.approve(requestId, request));
    }

    @Operation(summary = "Reject company join request", security = @SecurityRequirement(name = "bearerAuth"))
    @PatchMapping(UrlConstant.Company.REJECT_JOIN_REQUEST)
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    public ResponseEntity<?> reject(
            @PathVariable Long requestId,
            @RequestBody(required = false) @Valid CompanyJoinRequestReviewRequest request) {
        return VsResponseUtil.success(companyJoinRequestService.reject(requestId, request));
    }
}
