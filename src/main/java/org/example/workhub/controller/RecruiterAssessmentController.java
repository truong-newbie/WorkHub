package org.example.workhub.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.workhub.base.RestApiV1;
import org.example.workhub.base.VsResponseUtil;
import org.example.workhub.domain.dto.request.*;
import org.example.workhub.service.AssessmentTestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
@RestApiV1
public class RecruiterAssessmentController {

    AssessmentTestService assessmentTestService;

    @PostMapping("/recruiter/jobs/{jobId}/tests")
    @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    public ResponseEntity<?> createTest(@PathVariable Long jobId, @RequestBody @Valid AssessmentTestCreateRequest request) {
        return VsResponseUtil.success(HttpStatus.CREATED, assessmentTestService.createTest(jobId, request));
    }

    @PutMapping("/recruiter/tests/{testId}")
    @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateTest(@PathVariable Long testId, @RequestBody @Valid AssessmentTestUpdateRequest request) {
        return VsResponseUtil.success(assessmentTestService.updateTest(testId, request));
    }

    @DeleteMapping("/recruiter/tests/{testId}")
    @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteTest(@PathVariable Long testId) {
        assessmentTestService.deleteTest(testId);
        return VsResponseUtil.success(null);
    }

    @PutMapping("/recruiter/tests/{testId}/publish")
    @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    public ResponseEntity<?> publishTest(@PathVariable Long testId) {
        return VsResponseUtil.success(assessmentTestService.publishTest(testId));
    }

    @PutMapping("/recruiter/tests/{testId}/close")
    @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    public ResponseEntity<?> closeTest(@PathVariable Long testId) {
        return VsResponseUtil.success(assessmentTestService.closeTest(testId));
    }

    @PostMapping("/recruiter/tests/{testId}/questions")
    @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    public ResponseEntity<?> addQuestion(@PathVariable Long testId, @RequestBody @Valid AssessmentQuestionCreateRequest request) {
        return VsResponseUtil.success(HttpStatus.CREATED, assessmentTestService.addQuestion(testId, request));
    }

    @PutMapping("/recruiter/questions/{questionId}")
    @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateQuestion(@PathVariable Long questionId, @RequestBody @Valid AssessmentQuestionUpdateRequest request) {
        return VsResponseUtil.success(assessmentTestService.updateQuestion(questionId, request));
    }

    @DeleteMapping("/recruiter/questions/{questionId}")
    @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteQuestion(@PathVariable Long questionId) {
        assessmentTestService.deleteQuestion(questionId);
        return VsResponseUtil.success(null);
    }

    @PostMapping("/recruiter/tests/{testId}/assign")
    @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    public ResponseEntity<?> assignTest(@PathVariable Long testId, @RequestBody @Valid AssignTestRequest request) {
        return VsResponseUtil.success(HttpStatus.CREATED, assessmentTestService.assignTest(testId, request));
    }

    @GetMapping("/recruiter/tests/{testId}/assignments")
    @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    public ResponseEntity<?> getAssignments(@PathVariable Long testId) {
        return VsResponseUtil.success(assessmentTestService.getAssignments(testId));
    }

    @GetMapping("/recruiter/tests/{testId}/results")
    @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    public ResponseEntity<?> getResults(@PathVariable Long testId) {
        return VsResponseUtil.success(assessmentTestService.getResults(testId));
    }

    @GetMapping("/recruiter/test-assignments/{assignmentId}/answers")
    @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    public ResponseEntity<?> getAssignmentAnswers(@PathVariable Long assignmentId) {
        return VsResponseUtil.success(assessmentTestService.getAssignmentAnswers(assignmentId));
    }

    @PutMapping("/recruiter/answers/{answerId}/score")
    @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    public ResponseEntity<?> scoreEssayAnswer(@PathVariable Long answerId, @RequestBody @Valid ScoreEssayAnswerRequest request) {
        return VsResponseUtil.success(assessmentTestService.scoreEssayAnswer(answerId, request));
    }
}
