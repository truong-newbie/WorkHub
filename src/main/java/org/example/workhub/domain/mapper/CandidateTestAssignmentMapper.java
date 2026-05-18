package org.example.workhub.domain.mapper;

import org.example.workhub.domain.dto.response.CandidateTestAssignmentResponse;
import org.example.workhub.domain.dto.response.CandidateTestResultResponse;
import org.example.workhub.domain.dto.response.RecruiterTestResultResponse;
import org.example.workhub.domain.entity.CandidateAnswer;
import org.example.workhub.domain.entity.CandidateTestAssignment;
import org.mapstruct.Mapper;

import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring", uses = CandidateAnswerMapper.class)
public interface CandidateTestAssignmentMapper {

    default CandidateTestAssignmentResponse toResponse(CandidateTestAssignment assignment) {
        if (assignment == null) return null;
        return CandidateTestAssignmentResponse.builder()
                .id(assignment.getId())
                .testId(assignment.getTest() != null ? assignment.getTest().getId() : null)
                .testTitle(assignment.getTest() != null ? assignment.getTest().getTitle() : null)
                .testDescription(assignment.getTest() != null ? assignment.getTest().getDescription() : null)
                .applicationId(assignment.getApplication() != null ? assignment.getApplication().getId() : null)
                .candidateId(assignment.getCandidate() != null ? assignment.getCandidate().getId() : null)
                .candidateName(assignment.getCandidate() != null ? assignment.getCandidate().getUsername() : null)
                .candidateEmail(assignment.getCandidate() != null ? assignment.getCandidate().getEmail() : null)
                .status(assignment.getStatus())
                .durationMinutes(assignment.getTest() != null ? assignment.getTest().getDurationMinutes() : null)
                .testStartAt(assignment.getTest() != null ? assignment.getTest().getStartAt() : null)
                .testEndAt(assignment.getTest() != null ? assignment.getTest().getEndAt() : null)
                .startedAt(assignment.getStartedAt())
                .submittedAt(assignment.getSubmittedAt())
                .totalScore(assignment.getTotalScore())
                .maxScore(assignment.getMaxScore())
                .recruiterFeedback(assignment.getRecruiterFeedback())
                .build();
    }

    default List<CandidateTestAssignmentResponse> toResponses(List<CandidateTestAssignment> assignments) {
        if (assignments == null) return Collections.emptyList();
        return assignments.stream().map(this::toResponse).toList();
    }

    default RecruiterTestResultResponse toRecruiterResult(CandidateTestAssignment assignment) {
        if (assignment == null) return null;
        return RecruiterTestResultResponse.builder()
                .assignmentId(assignment.getId())
                .candidateId(assignment.getCandidate() != null ? assignment.getCandidate().getId() : null)
                .candidateName(assignment.getCandidate() != null ? assignment.getCandidate().getUsername() : null)
                .candidateEmail(assignment.getCandidate() != null ? assignment.getCandidate().getEmail() : null)
                .applicationId(assignment.getApplication() != null ? assignment.getApplication().getId() : null)
                .status(assignment.getStatus())
                .totalScore(assignment.getTotalScore())
                .maxScore(assignment.getMaxScore())
                .submittedAt(assignment.getSubmittedAt())
                .build();
    }

    default List<RecruiterTestResultResponse> toRecruiterResults(List<CandidateTestAssignment> assignments) {
        if (assignments == null) return Collections.emptyList();
        return assignments.stream().map(this::toRecruiterResult).toList();
    }

    default CandidateTestResultResponse toResult(CandidateTestAssignment assignment, List<CandidateAnswer> answers, CandidateAnswerMapper answerMapper) {
        if (assignment == null) return null;
        return CandidateTestResultResponse.builder()
                .assignmentId(assignment.getId())
                .testId(assignment.getTest() != null ? assignment.getTest().getId() : null)
                .testTitle(assignment.getTest() != null ? assignment.getTest().getTitle() : null)
                .status(assignment.getStatus())
                .totalScore(assignment.getTotalScore())
                .maxScore(assignment.getMaxScore())
                .submittedAt(assignment.getSubmittedAt())
                .recruiterFeedback(assignment.getRecruiterFeedback())
                .answers(answerMapper.toResponses(answers))
                .build();
    }
}
