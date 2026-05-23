package org.example.workhub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.workhub.base.RestApiV1;
import org.example.workhub.base.VsResponseUtil;
import org.example.workhub.constant.UrlConstant;
import org.example.workhub.domain.dto.request.CandidateJobPreferenceCreateRequest;
import org.example.workhub.domain.dto.request.CandidateJobPreferenceUpdateRequest;
import org.example.workhub.service.CandidateJobPreferenceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestApiV1
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
@SecurityRequirement(name = "bearerAuth")
public class CandidateJobPreferenceController {

    CandidateJobPreferenceService candidateJobPreferenceService;

    @Operation(summary = "Get candidate onboarding status")
    @GetMapping(UrlConstant.CandidateJobPreference.ONBOARDING_STATUS)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getOnboardingStatus() {
        return VsResponseUtil.success(candidateJobPreferenceService.getOnboardingStatus());
    }

    @Operation(summary = "Create candidate job preference")
    @PostMapping(UrlConstant.CandidateJobPreference.JOB_PREFERENCE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createPreference(@RequestBody @Valid CandidateJobPreferenceCreateRequest request) {
        return VsResponseUtil.success(HttpStatus.CREATED, candidateJobPreferenceService.createPreference(request));
    }

    @Operation(summary = "Get current candidate job preference")
    @GetMapping(UrlConstant.CandidateJobPreference.JOB_PREFERENCE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyPreference() {
        return VsResponseUtil.success(candidateJobPreferenceService.getMyPreference());
    }

    @Operation(summary = "Update current candidate job preference")
    @PutMapping(UrlConstant.CandidateJobPreference.JOB_PREFERENCE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updatePreference(@RequestBody @Valid CandidateJobPreferenceUpdateRequest request) {
        return VsResponseUtil.success(candidateJobPreferenceService.updatePreference(request));
    }
}
