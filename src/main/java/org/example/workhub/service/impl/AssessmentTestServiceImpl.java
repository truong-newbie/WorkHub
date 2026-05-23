package org.example.workhub.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.workhub.constant.*;
import org.example.workhub.domain.dto.request.*;
import org.example.workhub.domain.dto.response.*;
import org.example.workhub.domain.entity.*;
import org.example.workhub.domain.mapper.*;
import org.example.workhub.exception.BadRequestException;
import org.example.workhub.exception.ConflictException;
import org.example.workhub.exception.ForbiddenException;
import org.example.workhub.exception.NotFoundException;
import org.example.workhub.repository.*;
import org.example.workhub.service.AssessmentTestService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AssessmentTestServiceImpl implements AssessmentTestService {

    private final AssessmentTestRepository assessmentTestRepository;
    private final AssessmentQuestionRepository assessmentQuestionRepository;
    private final JobRepository jobRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final CandidateTestAssignmentRepository assignmentRepository;
    private final CandidateAnswerRepository answerRepository;
    private final AssessmentTestMapper assessmentTestMapper;
    private final AssessmentQuestionMapper assessmentQuestionMapper;
    private final CandidateTestAssignmentMapper assignmentMapper;
    private final CandidateAnswerMapper answerMapper;
    private final AssessmentSecuritySupport securitySupport;

    @Override
    public AssessmentTestResponse createTest(Long jobId, AssessmentTestCreateRequest request) {
        validateTestTime(request.getStartAt(), request.getEndAt());
        Job job = jobRepository.findByIdNotDeleted(jobId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Job.ERR_NOT_FOUND_ID, new String[]{String.valueOf(jobId)}));
        securitySupport.validateRecruiterOwnsJob(job);

        AssessmentTest test = assessmentTestMapper.toEntity(request);
        test.setStatus(AssessmentStatus.DRAFT);
        test.setDeleted(false);
        test.setJob(job);
        test.setRecruiter(securitySupport.currentUser());

        return toRecruiterTestResponse(assessmentTestRepository.save(test));
    }

    @Override
    public AssessmentTestResponse updateTest(Long testId, AssessmentTestUpdateRequest request) {
        AssessmentTest test = getTest(testId);
        securitySupport.validateRecruiterOwnsJob(test.getJob());

        boolean hasSubmission = assignmentRepository.existsByTestIdAndStatus(testId, AssignmentStatus.SUBMITTED);
        if (hasSubmission) {
            if (StringUtils.hasText(request.getTitle())
                    || request.getDurationMinutes() != null
                    || request.getStartAt() != null
                    || request.getEndAt() != null) {
                throw new BadRequestException(ErrorMessage.Assessment.ERR_ALREADY_SUBMITTED);
            }
            if (request.getDescription() != null) {
                test.setDescription(request.getDescription());
            }
            return toRecruiterTestResponse(assessmentTestRepository.save(test));
        }

        LocalDateTime startAt = request.getStartAt() != null ? request.getStartAt() : test.getStartAt();
        LocalDateTime endAt = request.getEndAt() != null ? request.getEndAt() : test.getEndAt();
        validateTestTime(startAt, endAt);
        assessmentTestMapper.updateEntity(request, test);
        return toRecruiterTestResponse(assessmentTestRepository.save(test));
    }

    @Override
    public void deleteTest(Long testId) {
        AssessmentTest test = getTest(testId);
        securitySupport.validateRecruiterOwnsJob(test.getJob());
        test.setDeleted(true);
        assessmentTestRepository.save(test);
    }

    @Override
    public AssessmentTestResponse publishTest(Long testId) {
        AssessmentTest test = getTest(testId);
        securitySupport.validateRecruiterOwnsJob(test.getJob());
        validatePublishable(test);
        test.setStatus(AssessmentStatus.PUBLISHED);
        return toRecruiterTestResponse(assessmentTestRepository.save(test));
    }

    @Override
    public AssessmentTestResponse closeTest(Long testId) {
        AssessmentTest test = getTest(testId);
        securitySupport.validateRecruiterOwnsJob(test.getJob());
        test.setStatus(AssessmentStatus.CLOSED);
        return toRecruiterTestResponse(assessmentTestRepository.save(test));
    }

    @Override
    public AssessmentQuestionResponse addQuestion(Long testId, AssessmentQuestionCreateRequest request) {
        AssessmentTest test = getTest(testId);
        securitySupport.validateRecruiterOwnsJob(test.getJob());
        validateQuestion(request.getType(), request.getOptions());

        AssessmentQuestion question = assessmentQuestionMapper.toEntity(request);
        question.setDeleted(false);
        question.setTest(test);
        applyOptions(question, request.getOptions());
        AssessmentQuestion saved = assessmentQuestionRepository.save(question);
        return assessmentQuestionMapper.toResponse(saved, true);
    }

    @Override
    public AssessmentQuestionResponse updateQuestion(Long questionId, AssessmentQuestionUpdateRequest request) {
        AssessmentQuestion question = getQuestion(questionId);
        securitySupport.validateRecruiterOwnsJob(question.getTest().getJob());

        QuestionType type = request.getType() != null ? request.getType() : question.getType();
        List<AssessmentOptionRequest> options = request.getOptions() != null ? request.getOptions() : toOptionRequests(question.getOptions());
        validateQuestion(type, options);

        assessmentQuestionMapper.updateEntity(request, question);
        if (request.getOptions() != null) {
            question.getOptions().clear();
            applyOptions(question, request.getOptions());
        }
        AssessmentQuestion saved = assessmentQuestionRepository.save(question);
        return assessmentQuestionMapper.toResponse(saved, true);
    }

    @Override
    public void deleteQuestion(Long questionId) {
        AssessmentQuestion question = getQuestion(questionId);
        securitySupport.validateRecruiterOwnsJob(question.getTest().getJob());
        question.setDeleted(true);
        assessmentQuestionRepository.save(question);
    }

    @Override
    public List<CandidateTestAssignmentResponse> assignTest(Long testId, AssignTestRequest request) {
        AssessmentTest test = getTest(testId);
        securitySupport.validateRecruiterOwnsJob(test.getJob());
        if (test.getStatus() != AssessmentStatus.PUBLISHED) {
            throw new BadRequestException(ErrorMessage.Assessment.ERR_TEST_NOT_PUBLISHED);
        }

        List<CandidateTestAssignment> createdAssignments = new ArrayList<>();
        for (Long applicationId : request.getApplicationIds()) {
            JobApplication application = jobApplicationRepository.findById(applicationId)
                    .orElseThrow(() -> new NotFoundException(ErrorMessage.Application.ERR_NOT_FOUND_ID));
            validateApplicationForAssignment(test, application);
            if (assignmentRepository.existsByTestIdAndApplicationId(testId, applicationId)) {
                throw new ConflictException(ErrorMessage.Assessment.ERR_ALREADY_ASSIGNED);
            }

            CandidateTestAssignment assignment = new CandidateTestAssignment();
            assignment.setTest(test);
            assignment.setApplication(application);
            assignment.setCandidate(application.getUser());
            assignment.setStatus(AssignmentStatus.ASSIGNED);
            assignment.setMaxScore(calculateMaxScore(test));
            assignment.setTotalScore(0D);
            createdAssignments.add(assignmentRepository.save(assignment));
        }
        return assignmentMapper.toResponses(createdAssignments);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CandidateTestAssignmentResponse> getAssignments(Long testId) {
        AssessmentTest test = getTest(testId);
        securitySupport.validateRecruiterOwnsJob(test.getJob());
        return assignmentMapper.toResponses(assignmentRepository.findByTestId(testId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecruiterTestResultResponse> getResults(Long testId) {
        AssessmentTest test = getTest(testId);
        securitySupport.validateRecruiterOwnsJob(test.getJob());
        return assignmentMapper.toRecruiterResults(assignmentRepository.findByTestId(testId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CandidateAnswerResponse> getAssignmentAnswers(Long assignmentId) {
        CandidateTestAssignment assignment = getAssignment(assignmentId);
        securitySupport.validateRecruiterOwnsJob(assignment.getTest().getJob());
        return answerMapper.toResponses(answerRepository.findByAssignmentId(assignmentId));
    }

    @Override
    public CandidateAnswerResponse scoreEssayAnswer(Long answerId, ScoreEssayAnswerRequest request) {
        CandidateAnswer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Assessment.ERR_ANSWER_NOT_FOUND));
        securitySupport.validateRecruiterOwnsJob(answer.getAssignment().getTest().getJob());
        if (answer.getQuestion().getType() != QuestionType.ESSAY) {
            throw new BadRequestException(ErrorMessage.Assessment.ERR_INVALID_QUESTION);
        }
        if (request.getScore() > answer.getQuestion().getScore()) {
            throw new BadRequestException(ErrorMessage.Assessment.ERR_INVALID_SCORE);
        }

        answer.setScore(request.getScore());
        answer.setFeedback(request.getFeedback());
        CandidateAnswer saved = answerRepository.save(answer);
        recalculateAssignmentScore(answer.getAssignment());
        return answerMapper.toResponse(saved);
    }

    private AssessmentTest getTest(Long testId) {
        return assessmentTestRepository.findByIdAndDeletedFalse(testId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Assessment.ERR_TEST_NOT_FOUND));
    }

    private AssessmentQuestion getQuestion(Long questionId) {
        return assessmentQuestionRepository.findByIdAndDeletedFalse(questionId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Assessment.ERR_QUESTION_NOT_FOUND));
    }

    private CandidateTestAssignment getAssignment(Long assignmentId) {
        return assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Assessment.ERR_ASSIGNMENT_NOT_FOUND));
    }

    private AssessmentTestResponse toRecruiterTestResponse(AssessmentTest test) {
        return assessmentTestMapper.toResponse(test, assessmentQuestionMapper, true);
    }

    private void validateTestTime(LocalDateTime startAt, LocalDateTime endAt) {
        if (startAt == null || endAt == null || !endAt.isAfter(startAt)) {
            throw new BadRequestException(ErrorMessage.Assessment.ERR_INVALID_QUESTION);
        }
    }

    private void validatePublishable(AssessmentTest test) {
        List<AssessmentQuestion> questions = test.getQuestions().stream()
                .filter(question -> !Boolean.TRUE.equals(question.getDeleted()))
                .toList();
        if (questions.isEmpty()) {
            throw new BadRequestException(ErrorMessage.Assessment.ERR_NO_QUESTIONS);
        }
        for (AssessmentQuestion question : questions) {
            if (question.getType() == QuestionType.MULTIPLE_CHOICE) {
                List<AssessmentOption> options = question.getOptions();
                if (options == null || options.size() < 2) {
                    throw new BadRequestException(ErrorMessage.Assessment.ERR_MC_OPTIONS_REQUIRED);
                }
                if (options.stream().noneMatch(option -> Boolean.TRUE.equals(option.getCorrect()))) {
                    throw new BadRequestException(ErrorMessage.Assessment.ERR_MC_CORRECT_REQUIRED);
                }
            }
        }
    }

    private void validateQuestion(QuestionType type, List<AssessmentOptionRequest> options) {
        if (type == QuestionType.MULTIPLE_CHOICE) {
            if (CollectionUtils.isEmpty(options) || options.size() < 2) {
                throw new BadRequestException(ErrorMessage.Assessment.ERR_MC_OPTIONS_REQUIRED);
            }
            if (options.stream().noneMatch(option -> Boolean.TRUE.equals(option.getCorrect()))) {
                throw new BadRequestException(ErrorMessage.Assessment.ERR_MC_CORRECT_REQUIRED);
            }
        }
    }

    private void applyOptions(AssessmentQuestion question, List<AssessmentOptionRequest> options) {
        if (CollectionUtils.isEmpty(options) || question.getType() == QuestionType.ESSAY) return;
        options.forEach(optionRequest -> question.getOptions().add(assessmentQuestionMapper.toOptionEntity(optionRequest, question)));
    }

    private List<AssessmentOptionRequest> toOptionRequests(List<AssessmentOption> options) {
        if (options == null) return List.of();
        return options.stream().map(option -> {
            AssessmentOptionRequest request = new AssessmentOptionRequest();
            request.setContent(option.getContent());
            request.setCorrect(option.getCorrect());
            return request;
        }).toList();
    }

    private void validateApplicationForAssignment(AssessmentTest test, JobApplication application) {
        if (Boolean.TRUE.equals(application.getDeleted())) {
            throw new NotFoundException(ErrorMessage.Application.ERR_NOT_FOUND_ID);
        }
        if (application.getJob() == null || !application.getJob().getId().equals(test.getJob().getId())) {
            throw new BadRequestException(ErrorMessage.Assessment.ERR_INVALID_ASSIGNMENT_APPLICATION);
        }
        if (application.getStatus() == StatusEnum.REJECTED) {
            throw new BadRequestException(ErrorMessage.Assessment.ERR_INVALID_ASSIGNMENT_APPLICATION);
        }
    }

    private Double calculateMaxScore(AssessmentTest test) {
        return test.getQuestions().stream()
                .filter(question -> !Boolean.TRUE.equals(question.getDeleted()))
                .mapToDouble(question -> question.getScore() == null ? 0D : question.getScore())
                .sum();
    }

    private void recalculateAssignmentScore(CandidateTestAssignment assignment) {
        List<CandidateAnswer> answers = answerRepository.findByAssignmentId(assignment.getId());
        double total = answers.stream()
                .filter(answer -> answer.getScore() != null)
                .mapToDouble(CandidateAnswer::getScore)
                .sum();
        assignment.setTotalScore(total);
        assignment.setMaxScore(calculateMaxScore(assignment.getTest()));
        assignmentRepository.save(assignment);
    }
}
