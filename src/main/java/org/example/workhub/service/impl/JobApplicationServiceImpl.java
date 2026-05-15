package org.example.workhub.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.workhub.constant.ErrorMessage;
import org.example.workhub.constant.RoleConstant;
import org.example.workhub.constant.StatusEnum;
import org.example.workhub.domain.dto.pagination.PagingMeta;
import org.example.workhub.domain.dto.pagination.PaginationResponseDto;
import org.example.workhub.domain.dto.request.ApplicationStatusRequest;
import org.example.workhub.domain.dto.request.JobApplicationRequest;
import org.example.workhub.domain.dto.response.JobApplicationResponse;
import org.example.workhub.domain.entity.Company;
import org.example.workhub.domain.entity.Job;
import org.example.workhub.domain.entity.JobApplication;
import org.example.workhub.domain.entity.User;
import org.example.workhub.domain.mapper.JobApplicationMapper;
import org.example.workhub.exception.BadRequestException;
import org.example.workhub.exception.ConflictException;
import org.example.workhub.exception.ForbiddenException;
import org.example.workhub.exception.NotFoundException;
import org.example.workhub.repository.CompanyRepository;
import org.example.workhub.repository.JobApplicationRepository;
import org.example.workhub.repository.JobRepository;
import org.example.workhub.repository.UserRepository;
import org.example.workhub.security.UserPrincipal;
import org.example.workhub.service.JobApplicationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class JobApplicationServiceImpl implements JobApplicationService {

    private final JobApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final JobApplicationMapper applicationMapper;

    // ========== Candidate Actions ==========

    @Override
    public JobApplicationResponse applyJob(Long jobId, JobApplicationRequest request) {
        UserPrincipal currentUser = getCurrentUserPrincipal();
        User user = getUserFromPrincipal(currentUser);

        // Find job
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Job.ERR_NOT_FOUND_ID, new String[]{String.valueOf(jobId)}));

        if (Boolean.TRUE.equals(job.getDeleted())) {
            throw new NotFoundException(ErrorMessage.Job.ERR_NOT_FOUND_ID, new String[]{String.valueOf(jobId)});
        }

        // Check if job is published
        if (!Boolean.TRUE.equals(job.getPublished())) {
            throw new BadRequestException(ErrorMessage.Job.ERR_PERMISSION_DENIED);
        }

        // Check if job is expired
        if (job.getExpiredAt() != null && job.getExpiredAt().isBefore(Instant.now())) {
            throw new BadRequestException(ErrorMessage.Job.ERR_EXPIRED_INVALID);
        }

        // Check if already applied
        if (applicationRepository.existsByJobIdAndUserIdAndDeletedFalse(jobId, currentUser.getId())) {
            throw new ConflictException(ErrorMessage.Application.ERR_ALREADY_APPLIED);
        }

        // Create application
        JobApplication application = new JobApplication();
        application.setUser(user);
        application.setJob(job);
        application.setCoverLetter(request.getCoverLetter());
        application.setStatus(StatusEnum.PENDING);
        application.setAppliedAt(Instant.now());

        JobApplication saved = applicationRepository.save(application);
        return applicationMapper.toResponse(saved);
    }

    @Override
    public void withdrawApplication(Long jobId) {
        UserPrincipal currentUser = getCurrentUserPrincipal();

        JobApplication application = applicationRepository.findByJobIdAndUserId(jobId, currentUser.getId())
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Application.ERR_NOT_FOUND_ID));

        // Can only withdraw PENDING applications
        if (application.getStatus() != StatusEnum.PENDING) {
            throw new BadRequestException(ErrorMessage.Application.ERR_CANNOT_WITHDRAW);
        }

        application.setDeleted(true);
        applicationRepository.save(application);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponseDto<JobApplicationResponse> getMyApplications(int page, int size) {
        UserPrincipal currentUser = getCurrentUserPrincipal();
        Pageable pageable = PageRequest.of(page, size);

        Page<JobApplication> applicationPage = applicationRepository.findByUserId(currentUser.getId(), pageable);

        PagingMeta pagingMeta = new PagingMeta(
                applicationPage.getTotalElements(),
                applicationPage.getTotalPages(),
                page + 1,
                size,
                "appliedAt",
                "DESC"
        );

        return new PaginationResponseDto<>(pagingMeta, applicationMapper.toResponses(applicationPage.getContent()));
    }

    // ========== Recruiter Actions ==========

    @Override
    @Transactional(readOnly = true)
    public PaginationResponseDto<JobApplicationResponse> getJobApplicants(Long jobId, int page, int size) {
        UserPrincipal currentUser = getCurrentUserPrincipal();
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Job.ERR_NOT_FOUND_ID, new String[]{String.valueOf(jobId)}));

        // Check permission - owner or admin
        validateJobAccess(job, currentUser);

        Pageable pageable = PageRequest.of(page, size);
        Page<JobApplication> applicationPage = applicationRepository.findByJobId(jobId, pageable);

        PagingMeta pagingMeta = new PagingMeta(
                applicationPage.getTotalElements(),
                applicationPage.getTotalPages(),
                page + 1,
                size,
                "appliedAt",
                "DESC"
        );

        return new PaginationResponseDto<>(pagingMeta, applicationMapper.toResponses(applicationPage.getContent()));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponseDto<JobApplicationResponse> getCompanyApplications(Long companyId, int page, int size) {
        UserPrincipal currentUser = getCurrentUserPrincipal();
        Pageable pageable = PageRequest.of(page, size);

        Page<JobApplication> applicationPage = applicationRepository.findByCompanyId(companyId, pageable);

        PagingMeta pagingMeta = new PagingMeta(
                applicationPage.getTotalElements(),
                applicationPage.getTotalPages(),
                page + 1,
                size,
                "appliedAt",
                "DESC"
        );

        return new PaginationResponseDto<>(pagingMeta, applicationMapper.toResponses(applicationPage.getContent()));
    }

    @Override
    public JobApplicationResponse updateApplicationStatus(Long applicationId, ApplicationStatusRequest request) {
        UserPrincipal currentUser = getCurrentUserPrincipal();

        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Application.ERR_NOT_FOUND_ID));

        // Check permission - recruiter of the job or admin
        validateJobAccess(application.getJob(), currentUser);

        // Validate status
        StatusEnum newStatus;
        try {
            newStatus = StatusEnum.valueOf(request.getStatus());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(ErrorMessage.INVALID_SOME_THING_FIELD);
        }

        // Only allow REVIEWING, APPROVED, REJECTED
        if (newStatus == StatusEnum.PENDING) {
            throw new BadRequestException(ErrorMessage.INVALID_SOME_THING_FIELD);
        }

        application.setStatus(newStatus);
        application.setReviewedAt(Instant.now());
        application.setReviewedBy(currentUser.getId());
        application.setReviewNote(request.getReviewNote());

        JobApplication updated = applicationRepository.save(application);
        return applicationMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasApplied(Long jobId, String userId) {
        return applicationRepository.existsByJobIdAndUserIdAndDeletedFalse(jobId, userId);
    }

    // ========== Helper Methods ==========

    private UserPrincipal getCurrentUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ForbiddenException(ErrorMessage.UNAUTHORIZED);
        }
        return (UserPrincipal) authentication.getPrincipal();
    }

    private User getUserFromPrincipal(UserPrincipal principal) {
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{principal.getId()}));
    }

    private void validateJobAccess(Job job, UserPrincipal currentUser) {
        if (isAdmin(currentUser)) return;

        // Check if user is recruiter of this job
        if (job.getRecruiter() != null && job.getRecruiter().getId().equals(currentUser.getId())) return;

        // Check if user's company owns this job
        User user = getUserFromPrincipal(currentUser);
        if (user.getCompany() != null && job.getCompany() != null &&
                user.getCompany().getId().equals(job.getCompany().getId())) return;

        throw new ForbiddenException(ErrorMessage.Job.ERR_PERMISSION_DENIED);
    }

    private boolean isAdmin(UserPrincipal currentUser) {
        if (currentUser.getAuthorities() == null) return false;
        return currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(RoleConstant.ADMIN) ||
                        a.getAuthority().equals("ROLE_ADMIN"));
    }
}