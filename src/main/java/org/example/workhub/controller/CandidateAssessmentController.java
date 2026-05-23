package org.example.workhub.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.workhub.base.RestApiV1;
import org.example.workhub.base.VsResponseUtil;
import org.example.workhub.domain.dto.request.SubmitTestRequest;
import org.example.workhub.service.CandidateAssessmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
@RestApiV1
public class CandidateAssessmentController {

    CandidateAssessmentService candidateAssessmentService;

    @GetMapping("/candidate/tests")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyTests() {
        return VsResponseUtil.success(candidateAssessmentService.getMyTests());
    }

    @GetMapping("/candidate/test-assignments/{assignmentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyAssignmentDetail(@PathVariable Long assignmentId) {
        return VsResponseUtil.success(candidateAssessmentService.getMyAssignmentDetail(assignmentId));
    }

    @PostMapping("/candidate/test-assignments/{assignmentId}/start")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> startTest(@PathVariable Long assignmentId) {
        return VsResponseUtil.success(candidateAssessmentService.startTest(assignmentId));
    }

    @PostMapping("/candidate/test-assignments/{assignmentId}/submit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> submitTest(@PathVariable Long assignmentId, @RequestBody @Valid SubmitTestRequest request) {
        return VsResponseUtil.success(candidateAssessmentService.submitTest(assignmentId, request));
    }

    @GetMapping("/candidate/test-assignments/{assignmentId}/result")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyResult(@PathVariable Long assignmentId) {
        return VsResponseUtil.success(candidateAssessmentService.getMyResult(assignmentId));
    }
}
