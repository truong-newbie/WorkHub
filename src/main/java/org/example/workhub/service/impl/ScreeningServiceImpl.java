package org.example.workhub.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.workhub.constant.ErrorMessage;
import org.example.workhub.constant.RoleConstant;
import org.example.workhub.constant.StatusEnum;
import org.example.workhub.domain.dto.response.AiResumeAnalysisResponse;
import org.example.workhub.domain.dto.response.ScreeningResultResponse;
import org.example.workhub.domain.entity.Job;
import org.example.workhub.domain.entity.JobApplication;
import org.example.workhub.domain.entity.ScreeningResult;
import org.example.workhub.domain.entity.Skill;
import org.example.workhub.domain.entity.User;
import org.example.workhub.domain.mapper.ScreeningResultMapper;
import org.example.workhub.exception.ForbiddenException;
import org.example.workhub.exception.InternalServerException;
import org.example.workhub.exception.NotFoundException;
import org.example.workhub.repository.JobApplicationRepository;
import org.example.workhub.repository.JobRepository;
import org.example.workhub.repository.ScreeningResultRepository;
import org.example.workhub.repository.UserRepository;
import org.example.workhub.security.UserPrincipal;
import org.example.workhub.service.AiWorkerClient;
import org.example.workhub.service.ScoreCalculatorService;
import org.example.workhub.service.ScreeningService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ScreeningServiceImpl implements ScreeningService {

    private final JobApplicationRepository applicationRepository;
    private final ScreeningResultRepository screeningResultRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final AiWorkerClient aiWorkerClient;
    private final ScoreCalculatorService scoreCalculatorService;
    private final ScreeningResultMapper screeningResultMapper;
    private final ObjectMapper objectMapper;

    @Override
    public ScreeningResultResponse screenApplication(Long applicationId) {
        UserPrincipal currentUser = getCurrentUserPrincipal();
        JobApplication application = findApplication(applicationId);
        validateJobAccess(application.getJob(), currentUser);

        if (application.getResume() == null) {
            throw new NotFoundException(ErrorMessage.Resume.ERR_NOT_FOUND);
        }

        Job job = application.getJob();
        List<String> jobSkills = job.getSkills() == null
                ? List.of()
                : job.getSkills().stream().map(Skill::getName).toList();
        AiResumeAnalysisResponse aiResponse = aiWorkerClient.analyzeResume(
                application.getResume(),
                buildJobDescription(job),
                jobSkills
        );

        ScreeningResult result = screeningResultRepository.findByApplicationId(applicationId)
                .orElseGet(ScreeningResult::new);
        result.setApplication(application);
        result.setSkillScore(aiResponse.getSkillScore());
        result.setSemanticScore(aiResponse.getSemanticScore());
        result.setExperienceScore(null);
        result.setEducationScore(null);
        result.setTotalScore(scoreCalculatorService.calculateTotalScore(aiResponse));
        result.setMatchedSkills(toJson(aiResponse.getMatchedSkills()));
        result.setMissingSkills(toJson(aiResponse.getMissingSkills()));
        result.setExtraSkills(toJson(aiResponse.getExtraSkills()));
        result.setAiSummary(aiResponse.getAiSummary());
        result.setRawText(aiResponse.getRawText());

        application.setStatus(StatusEnum.SCREENED);
        applicationRepository.save(application);
        return screeningResultMapper.toResponse(screeningResultRepository.save(result));
    }

    @Override
    @Transactional(readOnly = true)
    public ScreeningResultResponse getScreeningResult(Long applicationId) {
        UserPrincipal currentUser = getCurrentUserPrincipal();
        JobApplication application = findApplication(applicationId);
        validateJobAccess(application.getJob(), currentUser);
        ScreeningResult result = screeningResultRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Screening.ERR_NOT_FOUND));
        return screeningResultMapper.toResponse(result);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScreeningResultResponse> getJobScreeningResults(Long jobId) {
        UserPrincipal currentUser = getCurrentUserPrincipal();
        Job job = jobRepository.findByIdNotDeleted(jobId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Job.ERR_NOT_FOUND_ID, new String[]{String.valueOf(jobId)}));
        validateJobAccess(job, currentUser);
        return screeningResultMapper.toResponses(screeningResultRepository.findByApplicationJobIdOrderByTotalScoreDesc(jobId));
    }

    private JobApplication findApplication(Long applicationId) {
        return applicationRepository.findByIdAndDeletedFalse(applicationId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Application.ERR_NOT_FOUND_ID));
    }

    private String buildJobDescription(Job job) {
        return String.join("\n",
                nullToEmpty(job.getTitle()),
                nullToEmpty(job.getDescription()),
                nullToEmpty(job.getRequirement()),
                nullToEmpty(job.getBenefit())
        );
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String toJson(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values == null ? List.of() : values);
        } catch (JsonProcessingException e) {
            throw new InternalServerException(ErrorMessage.ERR_EXCEPTION_GENERAL);
        }
    }

    private UserPrincipal getCurrentUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ForbiddenException(ErrorMessage.UNAUTHORIZED);
        }
        return (UserPrincipal) authentication.getPrincipal();
    }

    private void validateJobAccess(Job job, UserPrincipal currentUser) {
        if (isAdmin(currentUser)) {
            return;
        }
        if (job.getRecruiter() != null && currentUser.getId().equals(job.getRecruiter().getId())) {
            return;
        }
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{currentUser.getId()}));
        if (user.getCompany() != null && job.getCompany() != null && user.getCompany().getId().equals(job.getCompany().getId())) {
            return;
        }
        throw new ForbiddenException(ErrorMessage.Application.ERR_RECRUITER_PERMISSION_DENIED);
    }

    private boolean isAdmin(UserPrincipal currentUser) {
        return currentUser.getAuthorities() != null
                && currentUser.getAuthorities().stream().anyMatch(authority -> RoleConstant.ADMIN.equals(authority.getAuthority()));
    }
}
