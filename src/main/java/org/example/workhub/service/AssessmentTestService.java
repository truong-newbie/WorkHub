package org.example.workhub.service;

import org.example.workhub.domain.dto.request.*;
import org.example.workhub.domain.dto.response.*;

import java.util.List;

public interface AssessmentTestService {

    AssessmentTestResponse createTest(Long jobId, AssessmentTestCreateRequest request);

    AssessmentTestResponse updateTest(Long testId, AssessmentTestUpdateRequest request);

    void deleteTest(Long testId);

    AssessmentTestResponse publishTest(Long testId);

    AssessmentTestResponse closeTest(Long testId);

    AssessmentQuestionResponse addQuestion(Long testId, AssessmentQuestionCreateRequest request);

    AssessmentQuestionResponse updateQuestion(Long questionId, AssessmentQuestionUpdateRequest request);

    void deleteQuestion(Long questionId);

    List<CandidateTestAssignmentResponse> assignTest(Long testId, AssignTestRequest request);

    List<CandidateTestAssignmentResponse> getAssignments(Long testId);

    List<RecruiterTestResultResponse> getResults(Long testId);

    List<CandidateAnswerResponse> getAssignmentAnswers(Long assignmentId);

    CandidateAnswerResponse scoreEssayAnswer(Long answerId, ScoreEssayAnswerRequest request);
}
