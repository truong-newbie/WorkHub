package org.example.workhub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.workhub.base.RestApiV1;
import org.example.workhub.base.VsResponseUtil;
import org.example.workhub.constant.UrlConstant;
import org.example.workhub.domain.dto.request.JobBehaviorTrackRequest;
import org.example.workhub.domain.dto.request.JobSearchTrackRequest;
import org.example.workhub.service.JobBehaviorService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestApiV1
@RequiredArgsConstructor
public class JobBehaviorController {

    private final JobBehaviorService jobBehaviorService;

    @Operation(summary = "Track candidate job view", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(UrlConstant.JobBehavior.TRACK_VIEW)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> trackView(@PathVariable Long jobId,
                                       @RequestBody(required = false) JobBehaviorTrackRequest request) {
        jobBehaviorService.trackView(jobId, request);
        return VsResponseUtil.success(null);
    }

    @Operation(summary = "Track candidate job click", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(UrlConstant.JobBehavior.TRACK_CLICK)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> trackClick(@PathVariable Long jobId,
                                        @RequestBody(required = false) JobBehaviorTrackRequest request) {
        jobBehaviorService.trackClick(jobId, request);
        return VsResponseUtil.success(null);
    }

    @Operation(summary = "Track candidate job search keyword", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(UrlConstant.JobBehavior.TRACK_SEARCH)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> trackSearch(@RequestBody @Valid JobSearchTrackRequest request) {
        jobBehaviorService.trackSearch(request);
        return VsResponseUtil.success(null);
    }

    @Operation(summary = "Get candidate recommendation behavior summary", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(UrlConstant.JobBehavior.BEHAVIOR_SUMMARY)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getBehaviorSummary() {
        return VsResponseUtil.success(jobBehaviorService.getBehaviorSummary());
    }
}
