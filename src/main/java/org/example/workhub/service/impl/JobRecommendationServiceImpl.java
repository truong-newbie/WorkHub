package org.example.workhub.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.workhub.constant.ErrorMessage;
import org.example.workhub.constant.WorkMode;
import org.example.workhub.domain.dto.pagination.PaginationResponseDto;
import org.example.workhub.domain.dto.pagination.PagingMeta;
import org.example.workhub.domain.dto.response.RecommendedJobResponse;
import org.example.workhub.domain.entity.CandidateJobPreference;
import org.example.workhub.domain.entity.Job;
import org.example.workhub.domain.entity.Skill;
import org.example.workhub.exception.BadRequestException;
import org.example.workhub.repository.CandidateJobPreferenceRepository;
import org.example.workhub.repository.JobApplicationRepository;
import org.example.workhub.repository.JobRepository;
import org.example.workhub.security.UserPrincipal;
import org.example.workhub.service.JobRecommendationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)
public class JobRecommendationServiceImpl implements JobRecommendationService {

    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9]+");

    JobRepository jobRepository;
    JobApplicationRepository jobApplicationRepository;
    CandidateJobPreferenceRepository candidateJobPreferenceRepository;

    @Override
    public PaginationResponseDto<RecommendedJobResponse> getLatestJobs(Pageable pageable) {
        Page<Job> page = jobRepository.findAvailablePublishedJobs(Instant.now(), pageable);
        List<RecommendedJobResponse> items = page.getContent().stream()
                .map(job -> buildBaseResponse(job, null, List.of(), List.of(), List.of()))
                .toList();
        return new PaginationResponseDto<>(buildMeta(page, pageable), items);
    }

    @Override
    public PaginationResponseDto<RecommendedJobResponse> getRecommendedJobs(Pageable pageable) {
        UserPrincipal principal = getCurrentUserPrincipal();
        CandidateJobPreference preference = candidateJobPreferenceRepository.findByCandidateId(principal.getId())
                .orElseThrow(() -> new BadRequestException(ErrorMessage.CandidateJobPreference.ERR_REQUIRED));

        List<RecommendedJobResponse> scoredJobs = jobRepository.findAvailablePublishedJobs(Instant.now()).stream()
                .filter(job -> !jobApplicationRepository.existsByJobIdAndUserIdAndDeletedFalse(job.getId(), principal.getId()))
                .map(job -> buildRecommendedResponse(job, preference))
                .sorted(Comparator
                        .comparing(RecommendedJobResponse::getMatchScore, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(RecommendedJobResponse::getCreatedDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        int pageNumber = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();
        int fromIndex = Math.min(pageNumber * pageSize, scoredJobs.size());
        int toIndex = Math.min(fromIndex + pageSize, scoredJobs.size());
        List<RecommendedJobResponse> items = scoredJobs.subList(fromIndex, toIndex);
        int totalPages = pageSize == 0 ? 0 : (int) Math.ceil((double) scoredJobs.size() / pageSize);

        PagingMeta meta = new PagingMeta(
                (long) scoredJobs.size(),
                totalPages,
                pageNumber + 1,
                pageSize,
                "matchScore",
                "DESC"
        );
        return new PaginationResponseDto<>(meta, items);
    }

    private RecommendedJobResponse buildRecommendedResponse(Job job, CandidateJobPreference preference) {
        MatchResult skillMatch = calculateSkillScore(job, preference);
        double titleScore = calculateTitleScore(job.getTitle(), preference.getDesiredJobTitle());
        double locationScore = calculateLocationScore(job.getLocation(), preference.getPreferredLocation(), preference.getWorkMode());
        double experienceScore = calculateExperienceScore(job.getExperienceYears(), preference.getExperienceYears());
        double salaryScore = calculateSalaryScore(job, preference);
        double totalScore = roundOneDecimal(skillMatch.score() * 0.4
                + titleScore * 0.2
                + locationScore * 0.15
                + experienceScore * 0.15
                + salaryScore * 0.1);

        List<String> reasons = new ArrayList<>();
        if (!skillMatch.matchedSkills().isEmpty()) {
            reasons.add("recommendation.reason.skill.match");
        }
        if (titleScore > 0) {
            reasons.add("recommendation.reason.title.match");
        }
        if (locationScore > 0) {
            reasons.add("recommendation.reason.location.match");
        }
        if (experienceScore >= 70) {
            reasons.add("recommendation.reason.experience.match");
        }
        if (salaryScore >= 70) {
            reasons.add("recommendation.reason.salary.match");
        }
        if (job.getEmploymentType() != null
                && preference.getEmploymentType() != null
                && normalize(job.getEmploymentType()).equals(normalize(preference.getEmploymentType().name()))) {
            reasons.add("recommendation.reason.employment.type.match");
        }

        return buildBaseResponse(job, totalScore, skillMatch.matchedSkills(), skillMatch.missingSkills(), reasons);
    }

    private RecommendedJobResponse buildBaseResponse(Job job,
                                                     Double matchScore,
                                                     List<String> matchedSkills,
                                                     List<String> missingSkills,
                                                     List<String> matchReasons) {
        return RecommendedJobResponse.builder()
                .jobId(job.getId())
                .title(job.getTitle())
                .companyName(job.getCompany() == null ? null : job.getCompany().getName())
                .location(job.getLocation())
                .salaryMin(job.getSalaryMin())
                .salaryMax(job.getSalaryMax())
                .experienceYears(job.getExperienceYears())
                .employmentType(job.getEmploymentType())
                .matchScore(matchScore)
                .matchedSkills(matchedSkills == null || matchedSkills.isEmpty() ? null : matchedSkills)
                .missingSkills(missingSkills == null || missingSkills.isEmpty() ? null : missingSkills)
                .matchReasons(matchReasons == null || matchReasons.isEmpty() ? null : matchReasons)
                .createdDate(job.getCreatedDate())
                .build();
    }

    private MatchResult calculateSkillScore(Job job, CandidateJobPreference preference) {
        List<Skill> jobSkills = job.getSkills() == null ? List.of() : job.getSkills();
        Set<Long> candidateSkillIds = new HashSet<>(preference.getSkills().stream().map(Skill::getId).toList());
        List<String> matchedSkills = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();

        for (Skill skill : jobSkills) {
            if (candidateSkillIds.contains(skill.getId())) {
                matchedSkills.add(skill.getName());
            } else {
                missingSkills.add(skill.getName());
            }
        }

        if (jobSkills.isEmpty()) {
            return new MatchResult(0.0, matchedSkills, missingSkills);
        }

        double score = ((double) matchedSkills.size() / jobSkills.size()) * 100;
        return new MatchResult(score, matchedSkills, missingSkills);
    }

    private double calculateTitleScore(String jobTitle, String desiredJobTitle) {
        String normalizedJobTitle = normalize(jobTitle);
        String normalizedDesiredTitle = normalize(desiredJobTitle);
        if (normalizedJobTitle.isBlank() || normalizedDesiredTitle.isBlank()) {
            return 0.0;
        }
        if (normalizedJobTitle.contains(normalizedDesiredTitle) || normalizedDesiredTitle.contains(normalizedJobTitle)) {
            return 100.0;
        }

        Set<String> jobWords = tokenize(normalizedJobTitle);
        Set<String> desiredWords = tokenize(normalizedDesiredTitle);
        jobWords.retainAll(desiredWords);
        return jobWords.isEmpty() ? 0.0 : 60.0;
    }

    private double calculateLocationScore(String jobLocation, String preferredLocation, WorkMode workMode) {
        String normalizedJobLocation = normalize(jobLocation);
        String normalizedPreferredLocation = normalize(preferredLocation);
        if (normalizedJobLocation.isBlank() || normalizedPreferredLocation.isBlank()) {
            return 0.0;
        }
        if (normalizedJobLocation.equals(normalizedPreferredLocation)) {
            return 100.0;
        }
        if (WorkMode.REMOTE.equals(workMode)
                && (normalizedPreferredLocation.contains("remote") || normalizedJobLocation.contains("remote"))) {
            return 100.0;
        }
        if (normalizedJobLocation.contains(normalizedPreferredLocation)
                || normalizedPreferredLocation.contains(normalizedJobLocation)) {
            return 80.0;
        }
        return 0.0;
    }

    private double calculateExperienceScore(Integer jobExperienceYears, Integer candidateExperienceYears) {
        if (jobExperienceYears == null) {
            return 70.0;
        }
        int candidateYears = candidateExperienceYears == null ? 0 : candidateExperienceYears;
        int missingYears = jobExperienceYears - candidateYears;
        if (missingYears <= 0) {
            return 100.0;
        }
        if (missingYears == 1) {
            return 70.0;
        }
        if (missingYears == 2) {
            return 40.0;
        }
        return 0.0;
    }

    private double calculateSalaryScore(Job job, CandidateJobPreference preference) {
        BigDecimal jobSalaryMin = parseSalary(job.getSalaryMin());
        BigDecimal jobSalaryMax = parseSalary(job.getSalaryMax());
        BigDecimal expectedMin = preference.getExpectedSalaryMin();
        BigDecimal expectedMax = preference.getExpectedSalaryMax();

        if (jobSalaryMin == null || jobSalaryMax == null || expectedMin == null || expectedMax == null) {
            return 50.0;
        }
        if (jobSalaryMin.compareTo(expectedMax) <= 0 && jobSalaryMax.compareTo(expectedMin) >= 0) {
            return 100.0;
        }
        if (jobSalaryMax.compareTo(expectedMin) < 0) {
            return 30.0;
        }
        if (jobSalaryMin.compareTo(expectedMax) > 0) {
            return 70.0;
        }
        return 50.0;
    }

    private BigDecimal parseSalary(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim().replace(",", ""));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }

    private Set<String> tokenize(String value) {
        String[] parts = NON_ALPHANUMERIC.split(value);
        Set<String> tokens = new HashSet<>();
        for (String part : parts) {
            if (!part.isBlank()) {
                tokens.add(part);
            }
        }
        return tokens;
    }

    private double roundOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private UserPrincipal getCurrentUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken
                || !(authentication.getPrincipal() instanceof UserPrincipal principal)
                || principal.getId() == null) {
            throw new BadRequestException(ErrorMessage.UNAUTHORIZED);
        }
        return principal;
    }

    private PagingMeta buildMeta(Page<Job> page, Pageable pageable) {
        String sortBy = pageable.getSort().stream()
                .findFirst()
                .map(order -> order.getProperty())
                .orElse("createdDate");
        String sortType = pageable.getSort().stream()
                .findFirst()
                .map(order -> order.getDirection().name())
                .orElse("DESC");
        return new PagingMeta(
                page.getTotalElements(),
                page.getTotalPages(),
                pageable.getPageNumber() + 1,
                pageable.getPageSize(),
                sortBy,
                sortType
        );
    }

    private record MatchResult(double score, List<String> matchedSkills, List<String> missingSkills) {
    }
}
