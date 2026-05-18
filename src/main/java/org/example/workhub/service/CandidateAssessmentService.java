package org.example.workhub.service;

import org.example.workhub.domain.dto.request.SubmitTestRequest;
import org.example.workhub.domain.dto.response.AssessmentTestResponse;
import org.example.workhub.domain.dto.response.CandidateTestAssignmentResponse;
import org.example.workhub.domain.dto.response.CandidateTestResultResponse;

import java.util.List;

public interface CandidateAssessmentService {

    List<CandidateTestAssignmentResponse> getMyTests();

    AssessmentTestResponse getMyAssignmentDetail(Long assignmentId);

    CandidateTestAssignmentResponse startTest(Long assignmentId);

    CandidateTestResultResponse submitTest(Long assignmentId, SubmitTestRequest request);

    CandidateTestResultResponse getMyResult(Long assignmentId);
}
