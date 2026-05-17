package org.example.workhub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.workhub.base.RestApiV1;
import org.example.workhub.base.VsResponseUtil;
import org.example.workhub.service.ScreeningService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@RestApiV1
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
@Tag(name = "ATS Screening Controller", description = "AI-powered ATS resume screening APIs")
public class ScreeningController {

    ScreeningService screeningService;

    @Operation(summary = "Run ATS screening for an application", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/recruiter/applications/{applicationId}/screen")
    @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    public ResponseEntity<?> screenApplication(@PathVariable Long applicationId) {
        return VsResponseUtil.success(screeningService.screenApplication(applicationId));
    }

    @Operation(summary = "Get ATS screening result for an application", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/recruiter/applications/{applicationId}/screening-result")
    @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    public ResponseEntity<?> getScreeningResult(@PathVariable Long applicationId) {
        return VsResponseUtil.success(screeningService.getScreeningResult(applicationId));
    }

    @Operation(summary = "Get ranked ATS screening results by job", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/recruiter/jobs/{jobId}/screening-results")
    @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    public ResponseEntity<?> getJobScreeningResults(@PathVariable Long jobId) {
        return VsResponseUtil.success(screeningService.getJobScreeningResults(jobId));
    }
}
