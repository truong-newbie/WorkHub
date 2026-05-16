package org.example.workhub.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.workhub.constant.ErrorMessage;
import org.example.workhub.constant.RoleConstant;
import org.example.workhub.constant.StatusEnum;
import org.example.workhub.domain.dto.pagination.PagingMeta;
import org.example.workhub.domain.dto.pagination.PaginationResponseDto;
import org.example.workhub.domain.dto.request.JobCreateRequest;
import org.example.workhub.domain.dto.request.JobFilterRequest;
import org.example.workhub.domain.dto.request.JobUpdateRequest;
import org.example.workhub.domain.dto.response.JobResponse;
import org.example.workhub.domain.dto.response.JobStatisticsResponse;
import org.example.workhub.domain.entity.Company;
import org.example.workhub.domain.entity.Job;
import org.example.workhub.domain.entity.Skill;
import org.example.workhub.domain.entity.User;
import org.example.workhub.domain.mapper.JobMapper;
import org.example.workhub.domain.specification.JobSpecification;
import org.example.workhub.exception.BadRequestException;
import org.example.workhub.exception.ForbiddenException;
import org.example.workhub.exception.NotFoundException;
import org.example.workhub.repository.CompanyRepository;
import org.example.workhub.repository.JobApplicationRepository;
import org.example.workhub.repository.JobRepository;
import org.example.workhub.repository.SkillRepository;
import org.example.workhub.repository.UserRepository;
import org.example.workhub.security.UserPrincipal;
import org.example.workhub.service.JobService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final JobApplicationRepository applicationRepository;
    private final CompanyRepository companyRepository;
    private final SkillRepository skillRepository;
    private final UserRepository userRepository;
    private final JobMapper jobMapper;

    // ========== CRUD ==========

    @Override
    public JobResponse createJob(JobCreateRequest request) {
        UserPrincipal currentUser = getCurrentUserPrincipal();
        validateRecruiterRole(currentUser);

        Company company = getRecruiterCompany(currentUser);

        // Build job
        Job job = new Job();
        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setRequirement(request.getRequirement());
        job.setBenefit(request.getBenefit());
        job.setLocation(request.getLocation());
        job.setSalaryMin(request.getSalaryMin());
        job.setSalaryMax(request.getSalaryMax());
        job.setNegotiableSalary(request.getNegotiableSalary());
        job.setLevel(request.getLevel());
        job.setEmploymentType(request.getEmploymentType());
        job.setExperienceYears(request.getExperienceYears());
        job.setQuantity(request.getQuantity());
        job.setPublished(request.getPublished() != null ? request.getPublished() : false);
        job.setCompany(company);
        job.setRecruiter(getUserFromPrincipal(currentUser));

        // Set dates
        if (request.getExpiredAt() != null) {
            job.setExpiredAt(request.getExpiredAt().atStartOfDay(ZoneId.systemDefault()).toInstant());
        }
        if (request.getStartDate() != null) {
            job.setStartDate(request.getStartDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
        }

        // Set skills
        if (request.getSkillIds() != null && !request.getSkillIds().isEmpty()) {
            List<Skill> skills = skillRepository.findAllById(request.getSkillIds());
            job.setSkills(skills);
        }

        // Generate slug
        job.setSlug(generateSlug(request.getTitle()));

        Job savedJob = jobRepository.save(job);
        return jobMapper.toResponse(savedJob);
    }

    @Override
    public JobResponse updateJob(Long id, JobUpdateRequest request) {
        UserPrincipal currentUser = getCurrentUserPrincipal();
        Job job = findJobByIdNotDeleted(id);

        // Check permission
        validateJobOwnership(job, currentUser);

        // Update fields
        if (request.getTitle() != null) {
            job.setTitle(request.getTitle());
            job.setSlug(generateSlug(request.getTitle()));
        }
        if (request.getDescription() != null) job.setDescription(request.getDescription());
        if (request.getRequirement() != null) job.setRequirement(request.getRequirement());
        if (request.getBenefit() != null) job.setBenefit(request.getBenefit());
        if (request.getLocation() != null) job.setLocation(request.getLocation());
        if (request.getSalaryMin() != null) job.setSalaryMin(request.getSalaryMin());
        if (request.getSalaryMax() != null) job.setSalaryMax(request.getSalaryMax());
        if (request.getNegotiableSalary() != null) job.setNegotiableSalary(request.getNegotiableSalary());
        if (request.getLevel() != null) job.setLevel(request.getLevel());
        if (request.getEmploymentType() != null) job.setEmploymentType(request.getEmploymentType());
        if (request.getExperienceYears() != null) job.setExperienceYears(request.getExperienceYears());
        if (request.getQuantity() != null) job.setQuantity(request.getQuantity());
        if (request.getPublished() != null) job.setPublished(request.getPublished());
        if (request.getExpiredAt() != null) job.setExpiredAt(request.getExpiredAt().atStartOfDay(ZoneId.systemDefault()).toInstant());
        if (request.getStartDate() != null) job.setStartDate(request.getStartDate().atStartOfDay(ZoneId.systemDefault()).toInstant());

        // Update skills
        if (request.getSkillIds() != null) {
            if (request.getSkillIds().isEmpty()) {
                job.setSkills(List.of());
            } else {
                List<Skill> skills = skillRepository.findAllById(request.getSkillIds());
                job.setSkills(skills);
            }
        }

        Job updatedJob = jobRepository.save(job);
        return jobMapper.toResponse(updatedJob);
    }

    @Override
    @Transactional(readOnly = true)
    public JobResponse getJobById(Long id) {
        Job job = jobRepository.findByIdNotDeleted(id)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Job.ERR_NOT_FOUND_ID, new String[]{String.valueOf(id)}));
        return jobMapper.toResponse(job);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponseDto<JobResponse> getAllJobs(JobFilterRequest filter) {
        Pageable pageable = buildPageable(filter);

        Specification<Job> spec = JobSpecification.search(filter.getKeyword())
                .and(JobSpecification.withFilters(filter));

        // Add skill filter if provided
        if (filter.getSkills() != null && !filter.getSkills().isEmpty()) {
            List<Long> skillIds = parseSkillIds(filter.getSkills());
            spec = spec.and(JobSpecification.hasSkill(skillIds));
        }

        Page<Job> jobPage = jobRepository.findAll(spec, pageable);
        List<JobResponse> jobResponses = jobMapper.toResponses(jobPage.getContent());

        PagingMeta pagingMeta = new PagingMeta(
                jobPage.getTotalElements(),
                jobPage.getTotalPages(),
                filter.getPage() + 1,
                filter.getSize(),
                filter.getSortBy() != null ? filter.getSortBy() : "createdDate",
                filter.getSortDir() != null ? filter.getSortDir() : "DESC"
        );

        return new PaginationResponseDto<>(pagingMeta, jobResponses);
    }

    @Override
    public void deleteJob(Long id) {
        UserPrincipal currentUser = getCurrentUserPrincipal();
        Job job = findJobByIdNotDeleted(id);

        // Check permission - owner or admin
        if (!isAdmin(currentUser) && !isJobOwner(job, currentUser)) {
            throw new ForbiddenException(ErrorMessage.Job.ERR_PERMISSION_DENIED);
        }

        job.setDeleted(true);
        job.setPublished(false);
        jobRepository.save(job);
    }

    // ========== Publish/Unpublish ==========

    @Override
    public JobResponse publishJob(Long id) {
        UserPrincipal currentUser = getCurrentUserPrincipal();
        Job job = findJobByIdNotDeleted(id);
        validateJobOwnership(job, currentUser);

        job.setPublished(true);
        Job updatedJob = jobRepository.save(job);
        return jobMapper.toResponse(updatedJob);
    }

    @Override
    public JobResponse unpublishJob(Long id) {
        UserPrincipal currentUser = getCurrentUserPrincipal();
        Job job = findJobByIdNotDeleted(id);
        validateJobOwnership(job, currentUser);

        job.setPublished(false);
        Job updatedJob = jobRepository.save(job);
        return jobMapper.toResponse(updatedJob);
    }

    // ========== Statistics ==========

    @Override
    @Transactional(readOnly = true)
    public JobStatisticsResponse getJobStatistics() {
        long totalJobs = jobRepository.countNotDeleted();
        long publishedJobs = jobRepository.countPublished();
        long draftJobs = jobRepository.countDraft();
        long totalApplications = applicationRepository.countTotal();
        long pendingApplications = applicationRepository.countByStatus(StatusEnum.PENDING);
        long approvedApplications = applicationRepository.countByStatus(StatusEnum.APPROVED);
        long rejectedApplications = applicationRepository.countByStatus(StatusEnum.REJECTED);

        // Jobs by level
        Map<String, Long> jobsByLevel = new HashMap<>();
        jobRepository.findAll().stream()
                .filter(j -> !Boolean.TRUE.equals(j.getDeleted()))
                .forEach(j -> {
                    String level = j.getLevel() != null ? j.getLevel().name() : "UNKNOWN";
                    jobsByLevel.merge(level, 1L, Long::sum);
                });

        // Jobs by location
        Map<String, Long> jobsByLocation = new HashMap<>();
        jobRepository.findAll().stream()
                .filter(j -> !Boolean.TRUE.equals(j.getDeleted()))
                .forEach(j -> {
                    String location = j.getLocation() != null ? j.getLocation() : "UNKNOWN";
                    jobsByLocation.merge(location, 1L, Long::sum);
                });

        return JobStatisticsResponse.builder()
                .totalJobs(totalJobs)
                .publishedJobs(publishedJobs)
                .draftJobs(draftJobs)
                .totalApplications(totalApplications)
                .pendingApplications(pendingApplications)
                .approvedApplications(approvedApplications)
                .rejectedApplications(rejectedApplications)
                .jobsByLevel(jobsByLevel)
                .jobsByLocation(jobsByLocation)
                .build();
    }

    // ========== Internal ==========

    @Override
    @Transactional(readOnly = true)
    public Job findById(Long id) {
        return findJobByIdNotDeleted(id);
    }

    // ========== Helper Methods ==========

    private Job findJobByIdNotDeleted(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Job.ERR_NOT_FOUND_ID, new String[]{String.valueOf(id)}));

        if (Boolean.TRUE.equals(job.getDeleted())) {
            throw new NotFoundException(ErrorMessage.Job.ERR_NOT_FOUND_ID, new String[]{String.valueOf(id)});
        }

        return job;
    }

    private UserPrincipal getCurrentUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ForbiddenException(ErrorMessage.UNAUTHORIZED);
        }
        return (UserPrincipal) authentication.getPrincipal();
    }

    private User getUserFromPrincipal(UserPrincipal principal) {
        return userRepository.findById(principal.getId())
                .filter(user -> !Boolean.TRUE.equals(user.getDeleted()))
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{principal.getId()}));
    }

    private void validateRecruiterRole(UserPrincipal currentUser) {
        if (currentUser.getAuthorities() == null) return;

        boolean isRecruiter = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(RoleConstant.RECRUITER) ||
                        a.getAuthority().equals("ROLE_RECRUITER"));
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(RoleConstant.ADMIN) ||
                        a.getAuthority().equals("ROLE_ADMIN"));

        if (!isRecruiter && !isAdmin) {
            throw new ForbiddenException(ErrorMessage.Job.ERR_PERMISSION_DENIED);
        }
    }

    private void validateJobOwnership(Job job, UserPrincipal currentUser) {
        if (isAdmin(currentUser)) return;
        if (job.getRecruiter() != null && job.getRecruiter().getId().equals(currentUser.getId())) return;
        throw new ForbiddenException(ErrorMessage.Job.ERR_PERMISSION_DENIED);
    }

    private boolean isJobOwner(Job job, UserPrincipal currentUser) {
        return job.getRecruiter() != null && job.getRecruiter().getId().equals(currentUser.getId());
    }

    private boolean isAdmin(UserPrincipal currentUser) {
        if (currentUser.getAuthorities() == null) return false;
        return currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(RoleConstant.ADMIN) ||
                        a.getAuthority().equals("ROLE_ADMIN"));
    }

    private Company getRecruiterCompany(UserPrincipal currentUser) {
        User recruiter = getUserFromPrincipal(currentUser);
        if (recruiter.getCompany() == null || Boolean.TRUE.equals(recruiter.getCompany().getDeleted())) {
            throw new BadRequestException(ErrorMessage.Company.ERR_OWNER_NOT_FOUND);
        }
        return recruiter.getCompany();
    }

    private String generateSlug(String title) {
        if (title == null) return "";
        String slug = title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
        // Ensure uniqueness
        String baseSlug = slug;
        int counter = 1;
        while (jobRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter++;
        }
        return slug;
    }

    private List<Long> parseSkillIds(String skills) {
        if (skills == null || skills.isEmpty()) return List.of();
        return java.util.Arrays.stream(skills.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    private Pageable buildPageable(JobFilterRequest filter) {
        Sort sort = "ASC".equalsIgnoreCase(filter.getSortDir())
                ? Sort.by(filter.getSortBy()).ascending()
                : Sort.by(filter.getSortBy()).descending();
        return PageRequest.of(filter.getPage(), filter.getSize(), sort);
    }
}
