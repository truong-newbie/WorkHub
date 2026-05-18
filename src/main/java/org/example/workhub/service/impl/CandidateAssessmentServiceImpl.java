package org.example.workhub.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.workhub.constant.AssessmentStatus;
import org.example.workhub.constant.AssignmentStatus;
import org.example.workhub.constant.ErrorMessage;
import org.example.workhub.constant.QuestionType;
import org.example.workhub.domain.dto.request.SubmitAnswerRequest;
import org.example.workhub.domain.dto.request.SubmitTestRequest;
import org.example.workhub.domain.dto.response.AssessmentTestResponse;
import org.example.workhub.domain.dto.response.CandidateTestAssignmentResponse;
import org.example.workhub.domain.dto.response.CandidateTestResultResponse;
import org.example.workhub.domain.entity.*;
import org.example.workhub.domain.mapper.AssessmentQuestionMapper;
import org.example.workhub.domain.mapper.AssessmentTestMapper;
import org.example.workhub.domain.mapper.CandidateAnswerMapper;
import org.example.workhub.domain.mapper.CandidateTestAssignmentMapper;
import org.example.workhub.exception.BadRequestException;
import org.example.workhub.exception.ConflictException;
import org.example.workhub.exception.ForbiddenException;
import org.example.workhub.exception.NotFoundException;
import org.example.workhub.repository.AssessmentOptionRepository;
import org.example.workhub.repository.AssessmentQuestionRepository;
import org.example.workhub.repository.CandidateAnswerRepository;
import org.example.workhub.repository.CandidateTestAssignmentRepository;
import org.example.workhub.security.UserPrincipal;
import org.example.workhub.service.CandidateAssessmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CandidateAssessmentServiceImpl implements CandidateAssessmentService {

    private final CandidateTestAssignmentRepository assignmentRepository;
    private final AssessmentQuestionRepository questionRepository;
    private final AssessmentOptionRepository optionRepository;
    private final CandidateAnswerRepository answerRepository;
    private final CandidateTestAssignmentMapper assignmentMapper;
    private final AssessmentTestMapper assessmentTestMapper;
    private final AssessmentQuestionMapper assessmentQuestionMapper;
    private final CandidateAnswerMapper answerMapper;
    private final AssessmentSecuritySupport securitySupport;

    @Override
    @Transactional(readOnly = true)
    public List<CandidateTestAssignmentResponse> getMyTests() {
        UserPrincipal principal = securitySupport.currentPrincipal();
        return assignmentMapper.toResponses(assignmentRepository.findByCandidateId(principal.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public AssessmentTestResponse getMyAssignmentDetail(Long assignmentId) {
        CandidateTestAssignment assignment = getOwnedAssignment(assignmentId);
        return assessmentTestMapper.toResponse(assignment.getTest(), assessmentQuestionMapper, false);
    }

    @Override
    public CandidateTestAssignmentResponse startTest(Long assignmentId) {
        CandidateTestAssignment assignment = getOwnedAssignment(assignmentId);
        AssessmentTest test = assignment.getTest();
        validateCanAccessOpenTest(test);
        if (assignment.getStatus() == AssignmentStatus.SUBMITTED) {
            throw new ConflictException(ErrorMessage.Assessment.ERR_ALREADY_SUBMITTED);
        }
        if (assignment.getStatus() == AssignmentStatus.EXPIRED) {
            throw new BadRequestException(ErrorMessage.Assessment.ERR_TEST_EXPIRED);
        }
        if (assignment.getStartedAt() == null) {
            assignment.setStartedAt(LocalDateTime.now());
            assignment.setStatus(AssignmentStatus.IN_PROGRESS);
        }
        return assignmentMapper.toResponse(assignmentRepository.save(assignment));
    }

    @Override
    public CandidateTestResultResponse submitTest(Long assignmentId, SubmitTestRequest request) {
        CandidateTestAssignment assignment = getOwnedAssignment(assignmentId);
        if (assignment.getStatus() == AssignmentStatus.SUBMITTED) {
            throw new ConflictException(ErrorMessage.Assessment.ERR_ALREADY_SUBMITTED);
        }
        if (assignment.getStatus() == AssignmentStatus.EXPIRED) {
            throw new BadRequestException(ErrorMessage.Assessment.ERR_TEST_EXPIRED);
        }
        if (assignment.getStatus() != AssignmentStatus.ASSIGNED && assignment.getStatus() != AssignmentStatus.IN_PROGRESS) {
            throw new BadRequestException(ErrorMessage.Assessment.ERR_INVALID_ASSIGNMENT_STATUS);
        }

        AssessmentTest test = assignment.getTest();
        validateCanAccessOpenTest(test);
        LocalDateTime now = LocalDateTime.now();
        if (assignment.getStartedAt() == null) {
            assignment.setStartedAt(now);
        }
        if (assignment.getStartedAt().plusMinutes(test.getDurationMinutes()).isBefore(now)) {
            assignment.setStatus(AssignmentStatus.EXPIRED);
            assignmentRepository.save(assignment);
            throw new BadRequestException(ErrorMessage.Assessment.ERR_DURATION_EXCEEDED);
        }
        if (test.getEndAt().isBefore(now)) {
            assignment.setStatus(AssignmentStatus.EXPIRED);
            assignmentRepository.save(assignment);
            throw new BadRequestException(ErrorMessage.Assessment.ERR_TEST_EXPIRED);
        }

        Set<Long> questionIds = new HashSet<>();
        for (SubmitAnswerRequest answerRequest : request.getAnswers()) {
            if (!questionIds.add(answerRequest.getQuestionId())) {
                throw new BadRequestException(ErrorMessage.Assessment.ERR_INVALID_QUESTION);
            }
            CandidateAnswer answer = buildAnswer(assignment, answerRequest);
            answerRepository.save(answer);
        }

        assignment.setStatus(AssignmentStatus.SUBMITTED);
        assignment.setSubmittedAt(now);
        assignment.setMaxScore(calculateMaxScore(test));
        assignment.setTotalScore(calculateCurrentScore(assignment.getId()));
        CandidateTestAssignment saved = assignmentRepository.save(assignment);
        return assignmentMapper.toResult(saved, answerRepository.findByAssignmentId(saved.getId()), answerMapper);
    }

    @Override
    @Transactional(readOnly = true)
    public CandidateTestResultResponse getMyResult(Long assignmentId) {
        CandidateTestAssignment assignment = getOwnedAssignment(assignmentId);
        return assignmentMapper.toResult(assignment, answerRepository.findByAssignmentId(assignmentId), answerMapper);
    }

    private CandidateTestAssignment getOwnedAssignment(Long assignmentId) {
        UserPrincipal principal = securitySupport.currentPrincipal();
        CandidateTestAssignment assignment = assignmentRepository.findByIdAndCandidateId(assignmentId, principal.getId())
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Assessment.ERR_ASSIGNMENT_NOT_FOUND));
        if (assignment.getCandidate() == null || !principal.getId().equals(assignment.getCandidate().getId())) {
            throw new ForbiddenException(ErrorMessage.Assessment.ERR_NOT_OWNER);
        }
        return assignment;
    }

    private void validateCanAccessOpenTest(AssessmentTest test) {
        LocalDateTime now = LocalDateTime.now();
        if (test.getStatus() != AssessmentStatus.PUBLISHED) {
            throw new BadRequestException(ErrorMessage.Assessment.ERR_TEST_NOT_PUBLISHED);
        }
        if (test.getStartAt().isAfter(now) || test.getEndAt().isBefore(now)) {
            throw new BadRequestException(ErrorMessage.Assessment.ERR_TEST_EXPIRED);
        }
    }

    private CandidateAnswer buildAnswer(CandidateTestAssignment assignment, SubmitAnswerRequest answerRequest) {
        if (answerRepository.existsByAssignmentIdAndQuestionId(assignment.getId(), answerRequest.getQuestionId())) {
            throw new ConflictException(ErrorMessage.Assessment.ERR_DUPLICATE_ANSWER);
        }
        AssessmentQuestion question = questionRepository.findByIdAndDeletedFalse(answerRequest.getQuestionId())
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Assessment.ERR_QUESTION_NOT_FOUND));
        if (question.getTest() == null || !question.getTest().getId().equals(assignment.getTest().getId())) {
            throw new BadRequestException(ErrorMessage.Assessment.ERR_INVALID_QUESTION);
        }

        CandidateAnswer answer = new CandidateAnswer();
        answer.setAssignment(assignment);
        answer.setQuestion(question);

        if (question.getType() == QuestionType.MULTIPLE_CHOICE) {
            if (answerRequest.getSelectedOptionId() == null) {
                throw new BadRequestException(ErrorMessage.Assessment.ERR_INVALID_OPTION);
            }
            AssessmentOption selectedOption = optionRepository.findByIdAndQuestionId(answerRequest.getSelectedOptionId(), question.getId())
                    .orElseThrow(() -> new BadRequestException(ErrorMessage.Assessment.ERR_INVALID_OPTION));
            answer.setSelectedOption(selectedOption);
            answer.setScore(Boolean.TRUE.equals(selectedOption.getCorrect()) ? question.getScore() : 0D);
        } else {
            answer.setEssayAnswer(answerRequest.getEssayAnswer());
            answer.setScore(null);
        }

        return answer;
    }

    private Double calculateMaxScore(AssessmentTest test) {
        return test.getQuestions().stream()
                .filter(question -> !Boolean.TRUE.equals(question.getDeleted()))
                .mapToDouble(question -> question.getScore() == null ? 0D : question.getScore())
                .sum();
    }

    private Double calculateCurrentScore(Long assignmentId) {
        return answerRepository.findByAssignmentId(assignmentId).stream()
                .filter(answer -> answer.getScore() != null)
                .mapToDouble(CandidateAnswer::getScore)
                .sum();
    }
}
