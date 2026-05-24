package org.example.workhub.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.workhub.config.RecommendationProperties;
import org.example.workhub.constant.ErrorMessage;
import org.example.workhub.constant.RecommendationReasonCode;
import org.example.workhub.constant.WorkMode;
import org.example.workhub.domain.dto.pagination.PaginationResponseDto;
import org.example.workhub.domain.dto.pagination.PagingMeta;
import org.example.workhub.domain.dto.response.RecommendationReasonResponse;
import org.example.workhub.domain.dto.response.RecommendedJobResponse;
import org.example.workhub.domain.entity.CandidateJobPreference;
import org.example.workhub.domain.entity.FavoriteJob;
import org.example.workhub.domain.entity.Job;
import org.example.workhub.domain.entity.JobApplication;
import org.example.workhub.domain.entity.JobClickHistory;
import org.example.workhub.domain.entity.JobRecommendationLog;
import org.example.workhub.domain.entity.JobSearchHistory;
import org.example.workhub.domain.entity.JobViewHistory;
import org.example.workhub.domain.entity.Skill;
import org.example.workhub.domain.entity.User;
import org.example.workhub.exception.BadRequestException;
import org.example.workhub.repository.CandidateJobPreferenceRepository;
import org.example.workhub.repository.FavoriteJobRepository;
import org.example.workhub.repository.JobApplicationRepository;
import org.example.workhub.repository.JobClickHistoryRepository;
import org.example.workhub.repository.JobRecommendationLogRepository;
import org.example.workhub.repository.JobRepository;
import org.example.workhub.repository.JobSearchHistoryRepository;
import org.example.workhub.repository.JobViewHistoryRepository;
import org.example.workhub.repository.UserRepository;
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JobRecommendationServiceImpl implements JobRecommendationService {

    static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9]+");

    JobRepository jobRepository;
    JobApplicationRepository jobApplicationRepository;
    CandidateJobPreferenceRepository candidateJobPreferenceRepository;
    FavoriteJobRepository favoriteJobRepository;
    JobViewHistoryRepository jobViewHistoryRepository;
    JobClickHistoryRepository jobClickHistoryRepository;
    JobSearchHistoryRepository jobSearchHistoryRepository;
    JobRecommendationLogRepository jobRecommendationLogRepository;
    UserRepository userRepository;
    RecommendationProperties recommendationProperties;

    @Override
    @Transactional(readOnly = true)
    public PaginationResponseDto<RecommendedJobResponse> getLatestJobs(Pageable pageable) {
        Page<Job> page = jobRepository.findAvailablePublishedJobs(Instant.now(), pageable);
        List<RecommendedJobResponse> items = page.getContent().stream()
                .map(job -> buildBaseResponse(job, null, List.of(), List.of(), List.of(), List.of(), false))
                .toList();
        return new PaginationResponseDto<>(buildMeta(page, pageable), items);
    }

    @Override
    @Transactional
    public PaginationResponseDto<RecommendedJobResponse> getRecommendedJobs(Pageable pageable) {
        return getRecommendedJobs(pageable, null, false, true);
    }

    @Override
    @Transactional
    public PaginationResponseDto<RecommendedJobResponse> getRecommendedJobs(Pageable pageable,
                                                                             String location,
                                                                             boolean refresh,
                                                                             boolean explain) {
        validateWeightConfig();
        UserPrincipal principal = getCurrentUserPrincipal();
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new BadRequestException(ErrorMessage.UNAUTHORIZED));
        CandidateJobPreference preference = candidateJobPreferenceRepository.findByCandidateId(principal.getId())
                .orElseThrow(() -> new BadRequestException(ErrorMessage.Recommendation.ERR_PREFERENCE_NOT_FOUND));

        List<Job> candidateJobs = jobRepository.findAvailablePublishedJobs(Instant.now()).stream()
                .filter(job -> isRecommendationEligible(job))
                .filter(job -> location == null || location.isBlank() || normalize(job.getLocation()).contains(normalize(location)))
                .filter(job -> !jobApplicationRepository.existsByJobIdAndUserIdAndDeletedFalse(job.getId(), principal.getId()))
                .limit(Math.max(1, recommendationProperties.getResult().getMaxCandidates()))
                .toList();

        LocalDateTime since = LocalDateTime.now().minusDays(recommendationProperties.getBehavior().getHistoryDays());
        Instant sinceInstant = since.atZone(ZoneId.systemDefault()).toInstant();
        InterestProfile interestProfile = buildInterestProfile(principal.getId(), since, sinceInstant);
        CollaborativeProfile collaborativeProfile = buildCollaborativeProfile(principal.getId(), preference, since, sinceInstant);

        List<ScoredJob> scoredJobs = new ArrayList<>();
        double maxBehaviorRaw = 0;
        double maxCollaborativeRaw = 0;

        for (Job job : candidateJobs) {
            ContentResult contentResult = calculateContentScore(job, preference);
            RawScore behaviorRaw = calculateBehaviorRaw(job, interestProfile);
            RawScore collaborativeRaw = calculateCollaborativeRaw(job, collaborativeProfile);
            maxBehaviorRaw = Math.max(maxBehaviorRaw, behaviorRaw.score());
            maxCollaborativeRaw = Math.max(maxCollaborativeRaw, collaborativeRaw.score());
            scoredJobs.add(new ScoredJob(job, contentResult, behaviorRaw, collaborativeRaw));
        }

        for (ScoredJob scoredJob : scoredJobs) {
            double behaviorScore = normalizeRaw(scoredJob.behaviorRaw().score(), maxBehaviorRaw);
            double collaborativeScore = normalizeRaw(scoredJob.collaborativeRaw().score(), maxCollaborativeRaw);
            scoredJob.setBehaviorScore(behaviorScore);
            scoredJob.setCollaborativeScore(collaborativeScore);
            scoredJob.setHybridScore(roundOneDecimal(
                    scoredJob.contentResult().score() * recommendationProperties.getWeights().getContent()
                            + behaviorScore * recommendationProperties.getWeights().getBehavior()
                            + collaborativeScore * recommendationProperties.getWeights().getCollaborative()
            ));
        }

        List<RecommendedJobResponse> responses = scoredJobs.stream()
                .sorted(Comparator
                        .comparing(ScoredJob::getHybridScore, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(scored -> scored.job().getCreatedDate(), Comparator.nullsLast(Comparator.reverseOrder())))
                .map(scored -> buildRecommendedResponse(scored, explain))
                .toList();

        int pageNumber = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();
        int fromIndex = Math.min(pageNumber * pageSize, responses.size());
        int toIndex = Math.min(fromIndex + pageSize, responses.size());
        List<RecommendedJobResponse> items = responses.subList(fromIndex, toIndex);
        saveRecommendationLogs(user, items);

        int totalPages = pageSize == 0 ? 0 : (int) Math.ceil((double) responses.size() / pageSize);
        PagingMeta meta = new PagingMeta(
                (long) responses.size(),
                totalPages,
                pageNumber + 1,
                pageSize,
                "hybridScore",
                "DESC"
        );
        return new PaginationResponseDto<>(meta, items);
    }

    private ContentResult calculateContentScore(Job job, CandidateJobPreference preference) {
        MatchResult skillMatch = calculateSkillScore(job, preference);
        double titleScore = calculateTitleScore(job.getTitle(), preference.getDesiredJobTitle());
        double locationWorkModeScore = calculateLocationWorkModeScore(job.getLocation(), preference.getPreferredLocation(), preference.getWorkMode());
        double experienceScore = calculateExperienceScore(job.getExperienceYears(), preference.getExperienceYears());
        double salaryScore = calculateSalaryScore(job, preference);
        double totalScore = roundOneDecimal(skillMatch.score() * 0.40
                + titleScore * 0.20
                + locationWorkModeScore * 0.15
                + experienceScore * 0.15
                + salaryScore * 0.10);

        Set<RecommendationReasonCode> reasonCodes = new LinkedHashSet<>();
        List<String> legacyReasons = new ArrayList<>();
        if (!skillMatch.matchedSkills().isEmpty()) {
            reasonCodes.add(RecommendationReasonCode.MATCHED_SKILL);
            legacyReasons.add("recommendation.reason.skill.match");
        }
        if (titleScore > 0) {
            reasonCodes.add(RecommendationReasonCode.MATCHED_TITLE);
            legacyReasons.add("recommendation.reason.title.match");
        }
        if (isLocationMatched(job.getLocation(), preference.getPreferredLocation())) {
            reasonCodes.add(RecommendationReasonCode.MATCHED_LOCATION);
            legacyReasons.add("recommendation.reason.location.match");
        }
        if (WorkMode.REMOTE.equals(preference.getWorkMode())) {
            reasonCodes.add(RecommendationReasonCode.MATCHED_WORK_MODE);
        }
        if (experienceScore >= 70) {
            reasonCodes.add(RecommendationReasonCode.MATCHED_EXPERIENCE);
            legacyReasons.add("recommendation.reason.experience.match");
        }
        if (salaryScore >= 70) {
            reasonCodes.add(RecommendationReasonCode.MATCHED_SALARY);
            legacyReasons.add("recommendation.reason.salary.match");
        }
        if (job.getEmploymentType() != null
                && preference.getEmploymentType() != null
                && normalize(job.getEmploymentType()).equals(normalize(preference.getEmploymentType().name()))) {
            reasonCodes.add(RecommendationReasonCode.MATCHED_EMPLOYMENT_TYPE);
            legacyReasons.add("recommendation.reason.employment.type.match");
        }

        return new ContentResult(totalScore, skillMatch.matchedSkills(), skillMatch.missingSkills(), reasonCodes, legacyReasons);
    }

    private RawScore calculateBehaviorRaw(Job job, InterestProfile profile) {
        if (profile.totalWeight() <= 0) {
            return new RawScore(0, Set.of());
        }
        double score = 0;
        Set<RecommendationReasonCode> reasons = new LinkedHashSet<>();

        if (job.getSkills() != null) {
            for (Skill skill : job.getSkills()) {
                double weight = profile.skillInterest().getOrDefault(normalize(skill.getName()), 0.0);
                if (weight > 0) {
                    score += weight;
                    reasons.add(RecommendationReasonCode.BASED_ON_VIEW_HISTORY);
                    reasons.add(RecommendationReasonCode.BASED_ON_CLICK_HISTORY);
                    reasons.add(RecommendationReasonCode.BASED_ON_SAVED_JOBS);
                    reasons.add(RecommendationReasonCode.BASED_ON_APPLIED_JOBS);
                }
            }
        }

        for (String token : tokenize(job.getTitle())) {
            double weight = profile.titleKeywordInterest().getOrDefault(token, 0.0);
            if (weight > 0) {
                score += weight;
                reasons.add(RecommendationReasonCode.BASED_ON_SEARCH_KEYWORD);
            }
        }

        double locationWeight = profile.locationInterest().getOrDefault(normalize(job.getLocation()), 0.0);
        if (locationWeight > 0) {
            score += locationWeight;
            reasons.add(RecommendationReasonCode.BASED_ON_VIEW_HISTORY);
        }

        Long companyId = job.getCompany() == null ? null : job.getCompany().getId();
        if (companyId != null) {
            double companyWeight = profile.companyInterest().getOrDefault(companyId, 0.0);
            if (companyWeight > 0) {
                score += companyWeight;
                reasons.add(RecommendationReasonCode.BASED_ON_CLICK_HISTORY);
            }
        }

        return new RawScore(score, reasons);
    }

    private RawScore calculateCollaborativeRaw(Job job, CollaborativeProfile profile) {
        JobCollaborativeStats stats = profile.jobStats().get(job.getId());
        if (stats == null || stats.rawScore() <= 0) {
            return new RawScore(0, Set.of());
        }
        Set<RecommendationReasonCode> reasons = new LinkedHashSet<>();
        if (stats.appliedWeight() > 0) {
            reasons.add(RecommendationReasonCode.USERS_LIKE_YOU_APPLIED);
        }
        if (stats.savedWeight() > 0) {
            reasons.add(RecommendationReasonCode.USERS_LIKE_YOU_SAVED);
        }
        return new RawScore(stats.rawScore(), reasons);
    }

    private InterestProfile buildInterestProfile(String userId, LocalDateTime since, Instant sinceInstant) {
        RecommendationProperties.Behavior weights = recommendationProperties.getBehavior();
        InterestProfile profile = new InterestProfile();

        jobViewHistoryRepository.findByUserIdSince(userId, since)
                .forEach(history -> addJobInterest(profile, history.getJob(), weights.getViewWeight()));
        jobClickHistoryRepository.findByUserIdSince(userId, since)
                .forEach(history -> addJobInterest(profile, history.getJob(), weights.getClickWeight()));
        favoriteJobRepository.findActiveByUserIdSince(userId, sinceInstant)
                .forEach(favorite -> addJobInterest(profile, favorite.getJob(), weights.getSaveWeight()));
        jobApplicationRepository.findActiveByUserIdSince(userId, sinceInstant)
                .forEach(application -> addJobInterest(profile, application.getJob(), weights.getApplyWeight()));
        jobSearchHistoryRepository.findByUserIdSince(userId, since)
                .forEach(search -> addKeywordInterest(profile.titleKeywordInterest(), search.getKeyword(), weights.getSearchKeywordWeight()));

        return profile;
    }

    private CollaborativeProfile buildCollaborativeProfile(String userId,
                                                           CandidateJobPreference preference,
                                                           LocalDateTime since,
                                                           Instant sinceInstant) {
        Set<Long> currentSkills = preference.getSkills() == null ? Set.of() : preference.getSkills().stream()
                .map(Skill::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<JobApplication> allApplications = jobApplicationRepository.findAllActiveSince(sinceInstant);
        List<FavoriteJob> allFavorites = favoriteJobRepository.findAllActiveSince(sinceInstant);
        List<JobViewHistory> allViews = jobViewHistoryRepository.findAllSince(since);
        List<JobClickHistory> allClicks = jobClickHistoryRepository.findAllSince(since);

        Map<String, Set<Long>> appliedByUser = groupJobIdsByUser(allApplications);
        Map<String, Set<Long>> savedByUser = groupJobIdsByUser(allFavorites);
        Map<String, Set<Long>> behaviorByUser = groupBehaviorJobIdsByUser(allViews, allClicks);
        Map<String, Double> similarUsers = candidateJobPreferenceRepository.findAll().stream()
                .filter(other -> other.getCandidate() != null && !userId.equals(other.getCandidate().getId()))
                .map(other -> {
                    String otherUserId = other.getCandidate().getId();
                    Set<Long> otherSkills = other.getSkills() == null ? Set.of() : other.getSkills().stream()
                            .map(Skill::getId)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());
                    double similarity = jaccard(currentSkills, otherSkills) * 0.40
                            + jaccard(appliedByUser.get(userId), appliedByUser.get(otherUserId)) * 0.30
                            + jaccard(savedByUser.get(userId), savedByUser.get(otherUserId)) * 0.20
                            + jaccard(behaviorByUser.get(userId), behaviorByUser.get(otherUserId)) * 0.10;
                    return new SimilarUser(otherUserId, similarity);
                })
                .filter(similar -> similar.similarity() >= recommendationProperties.getCollaborative().getMinSimilarity())
                .sorted(Comparator.comparing(SimilarUser::similarity).reversed())
                .limit(recommendationProperties.getCollaborative().getSimilarUserLimit())
                .collect(Collectors.toMap(SimilarUser::userId, SimilarUser::similarity, (a, b) -> a, java.util.LinkedHashMap::new));

        Map<Long, JobCollaborativeStats> jobStats = new HashMap<>();
        for (Map.Entry<String, Double> entry : similarUsers.entrySet()) {
            String similarUserId = entry.getKey();
            double similarity = entry.getValue();
            addCollaborativeStats(jobStats, appliedByUser.get(similarUserId), similarity, 10, true, false);
            addCollaborativeStats(jobStats, savedByUser.get(similarUserId), similarity, 5, false, true);
            addCollaborativeStats(jobStats, behaviorByUser.get(similarUserId), similarity, 2, false, false);
        }

        return new CollaborativeProfile(jobStats);
    }

    private RecommendedJobResponse buildRecommendedResponse(ScoredJob scoredJob, boolean explain) {
        Set<RecommendationReasonCode> reasonCodes = new LinkedHashSet<>();
        reasonCodes.addAll(scoredJob.contentResult().reasonCodes());
        reasonCodes.addAll(scoredJob.behaviorRaw().reasons());
        reasonCodes.addAll(scoredJob.collaborativeRaw().reasons());

        List<RecommendationReasonResponse> reasons = reasonCodes.stream()
                .map(code -> RecommendationReasonResponse.builder()
                        .code(code)
                        .text(toReasonText(code))
                        .build())
                .toList();

        return buildBaseResponse(
                scoredJob.job(),
                scoredJob.getHybridScore(),
                scoredJob.contentResult().matchedSkills(),
                scoredJob.contentResult().missingSkills(),
                scoredJob.contentResult().legacyReasons(),
                reasonCodes.stream().map(Enum::name).toList(),
                explain
        ).toBuilder()
                .contentScore(scoredJob.contentResult().score())
                .behaviorScore(scoredJob.getBehaviorScore())
                .collaborativeScore(scoredJob.getCollaborativeScore())
                .hybridScore(scoredJob.getHybridScore())
                .reasonCodes(reasonCodes.stream().map(Enum::name).toList())
                .reasons(explain ? reasons : null)
                .reasonText(explain ? buildReasonText(scoredJob, reasonCodes) : null)
                .build();
    }

    private RecommendedJobResponse buildBaseResponse(Job job,
                                                     Double matchScore,
                                                     List<String> matchedSkills,
                                                     List<String> missingSkills,
                                                     List<String> matchReasons,
                                                     List<String> reasonCodes,
                                                     boolean explain) {
        Long companyId = job.getCompany() == null ? null : job.getCompany().getId();
        return RecommendedJobResponse.builder()
                .jobId(job.getId())
                .id(job.getId())
                .title(job.getTitle())
                .slug(job.getSlug())
                .companyId(companyId)
                .companyName(job.getCompany() == null ? null : job.getCompany().getName())
                .companyLogo(job.getCompany() == null ? null : job.getCompany().getLogo())
                .location(job.getLocation())
                .salaryMin(job.getSalaryMin())
                .salaryMax(job.getSalaryMax())
                .negotiableSalary(job.getNegotiableSalary())
                .level(job.getLevel() == null ? null : job.getLevel().name())
                .experienceYears(job.getExperienceYears())
                .employmentType(job.getEmploymentType())
                .matchScore(matchScore)
                .hybridScore(matchScore)
                .skillNames(job.getSkills() == null ? List.of() : job.getSkills().stream().map(Skill::getName).toList())
                .expiredAt(job.getExpiredAt())
                .matchedSkills(matchedSkills == null || matchedSkills.isEmpty() ? null : matchedSkills)
                .missingSkills(missingSkills == null || missingSkills.isEmpty() ? null : missingSkills)
                .matchReasons(matchReasons == null || matchReasons.isEmpty() ? null : matchReasons)
                .reasonCodes(reasonCodes == null || reasonCodes.isEmpty() ? null : reasonCodes)
                .reasonText(explain && reasonCodes != null && !reasonCodes.isEmpty() ? "Matched recommendation signals: " + String.join(", ", reasonCodes) : null)
                .createdDate(job.getCreatedDate())
                .build();
    }

    private MatchResult calculateSkillScore(Job job, CandidateJobPreference preference) {
        List<Skill> jobSkills = job.getSkills() == null ? List.of() : job.getSkills();
        Set<Long> candidateSkillIds = preference.getSkills() == null ? Set.of() : preference.getSkills().stream()
                .map(Skill::getId)
                .collect(Collectors.toSet());
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
            return new MatchResult(50.0, matchedSkills, missingSkills);
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
        return jobWords.isEmpty() ? 0.0 : Math.min(100.0, ((double) jobWords.size() / desiredWords.size()) * 100);
    }

    private double calculateLocationWorkModeScore(String jobLocation, String preferredLocation, WorkMode workMode) {
        double score = isLocationMatched(jobLocation, preferredLocation) ? 50.0 : 0.0;
        if (WorkMode.REMOTE.equals(workMode) && normalize(jobLocation).contains("remote")) {
            score += 50.0;
        } else if (workMode == null) {
            score += 25.0;
        }
        return Math.min(100.0, score);
    }

    private boolean isLocationMatched(String jobLocation, String preferredLocation) {
        String normalizedJobLocation = normalize(jobLocation);
        String normalizedPreferredLocation = normalize(preferredLocation);
        return !normalizedJobLocation.isBlank()
                && !normalizedPreferredLocation.isBlank()
                && (normalizedJobLocation.equals(normalizedPreferredLocation)
                || normalizedJobLocation.contains(normalizedPreferredLocation)
                || normalizedPreferredLocation.contains(normalizedJobLocation));
    }

    private double calculateExperienceScore(Integer jobExperienceYears, Integer candidateExperienceYears) {
        if (jobExperienceYears == null || jobExperienceYears <= 0) {
            return 100.0;
        }
        int candidateYears = candidateExperienceYears == null ? 0 : candidateExperienceYears;
        if (candidateYears >= jobExperienceYears) {
            return 100.0;
        }
        return Math.max(0, ((double) candidateYears / jobExperienceYears) * 100);
    }

    private double calculateSalaryScore(Job job, CandidateJobPreference preference) {
        if (Boolean.TRUE.equals(job.getNegotiableSalary())) {
            return 80.0;
        }

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

    private void addJobInterest(InterestProfile profile, Job job, double weight) {
        if (job == null || weight <= 0) {
            return;
        }
        profile.addTotalWeight(weight);
        if (job.getSkills() != null) {
            for (Skill skill : job.getSkills()) {
                addWeight(profile.skillInterest(), normalize(skill.getName()), weight);
            }
        }
        addKeywordInterest(profile.titleKeywordInterest(), job.getTitle(), weight);
        addWeight(profile.locationInterest(), normalize(job.getLocation()), weight);
        if (job.getCompany() != null) {
            profile.companyInterest().merge(job.getCompany().getId(), weight, Double::sum);
        }
    }

    private void addKeywordInterest(Map<String, Double> target, String value, double weight) {
        for (String token : tokenize(value)) {
            if (token.length() >= 2) {
                addWeight(target, token, weight);
            }
        }
    }

    private void addWeight(Map<String, Double> map, String key, double weight) {
        if (key == null || key.isBlank()) {
            return;
        }
        map.merge(key, weight, Double::sum);
    }

    private Map<String, Set<Long>> groupJobIdsByUser(List<? extends Object> rows) {
        Map<String, Set<Long>> result = new HashMap<>();
        for (Object row : rows) {
            String userId = null;
            Job job = null;
            if (row instanceof JobApplication application) {
                userId = application.getUser() == null ? null : application.getUser().getId();
                job = application.getJob();
            } else if (row instanceof FavoriteJob favorite) {
                userId = favorite.getUser() == null ? null : favorite.getUser().getId();
                job = favorite.getJob();
            }
            if (userId != null && job != null && job.getId() != null) {
                result.computeIfAbsent(userId, ignored -> new HashSet<>()).add(job.getId());
            }
        }
        return result;
    }

    private Map<String, Set<Long>> groupBehaviorJobIdsByUser(List<JobViewHistory> views, List<JobClickHistory> clicks) {
        Map<String, Set<Long>> result = new HashMap<>();
        for (JobViewHistory view : views) {
            addBehaviorJob(result, view.getUser() == null ? null : view.getUser().getId(), view.getJob());
        }
        for (JobClickHistory click : clicks) {
            addBehaviorJob(result, click.getUser() == null ? null : click.getUser().getId(), click.getJob());
        }
        return result;
    }

    private void addBehaviorJob(Map<String, Set<Long>> target, String userId, Job job) {
        if (userId != null && job != null && job.getId() != null) {
            target.computeIfAbsent(userId, ignored -> new HashSet<>()).add(job.getId());
        }
    }

    private void addCollaborativeStats(Map<Long, JobCollaborativeStats> target,
                                       Set<Long> jobIds,
                                       double similarity,
                                       double actionWeight,
                                       boolean applied,
                                       boolean saved) {
        if (jobIds == null || jobIds.isEmpty()) {
            return;
        }
        for (Long jobId : jobIds) {
            JobCollaborativeStats stats = target.computeIfAbsent(jobId, ignored -> new JobCollaborativeStats());
            double weighted = similarity * actionWeight;
            stats.addRawScore(weighted);
            if (applied) {
                stats.addAppliedWeight(weighted);
            }
            if (saved) {
                stats.addSavedWeight(weighted);
            }
        }
    }

    private boolean isRecommendationEligible(Job job) {
        if (job == null || Boolean.TRUE.equals(job.getDeleted()) || !Boolean.TRUE.equals(job.getPublished())) {
            return false;
        }
        if (job.getExpiredAt() != null && job.getExpiredAt().isBefore(Instant.now())) {
            return false;
        }
        return job.getCompany() == null
                || (Boolean.TRUE.equals(job.getCompany().getActive()) && Boolean.TRUE.equals(job.getCompany().getVerified()));
    }

    private void validateWeightConfig() {
        double content = recommendationProperties.getWeights().getContent();
        double behavior = recommendationProperties.getWeights().getBehavior();
        double collaborative = recommendationProperties.getWeights().getCollaborative();
        double total = content + behavior + collaborative;
        if (content < 0 || behavior < 0 || collaborative < 0 || total <= 0) {
            throw new BadRequestException(ErrorMessage.Recommendation.ERR_INVALID_WEIGHT_CONFIG);
        }
    }

    private void saveRecommendationLogs(User user, List<RecommendedJobResponse> items) {
        List<JobRecommendationLog> logs = items.stream()
                .map(item -> {
                    JobRecommendationLog log = new JobRecommendationLog();
                    log.setUser(user);
                    Job job = new Job();
                    job.setId(item.getJobId());
                    log.setJob(job);
                    log.setContentScore(item.getContentScore());
                    log.setBehaviorScore(item.getBehaviorScore());
                    log.setCollaborativeScore(item.getCollaborativeScore());
                    log.setHybridScore(item.getHybridScore());
                    log.setReasonCodes(item.getReasonCodes() == null ? null : String.join(",", item.getReasonCodes()));
                    log.setGeneratedAt(LocalDateTime.now());
                    return log;
                })
                .toList();
        jobRecommendationLogRepository.saveAll(logs);
    }

    private String toReasonText(RecommendationReasonCode code) {
        return switch (code) {
            case MATCHED_SKILL -> "Job skills overlap with your preference skills";
            case MATCHED_TITLE -> "Job title matches your desired title";
            case MATCHED_LOCATION -> "Job location matches your preferred location";
            case MATCHED_WORK_MODE -> "Work mode matches your preference";
            case MATCHED_EMPLOYMENT_TYPE -> "Employment type matches your preference";
            case MATCHED_EXPERIENCE -> "Required experience fits your profile";
            case MATCHED_SALARY -> "Salary range fits your expectation";
            case BASED_ON_VIEW_HISTORY -> "Similar to jobs you viewed";
            case BASED_ON_CLICK_HISTORY -> "Similar to jobs you clicked";
            case BASED_ON_SAVED_JOBS -> "Similar to jobs you saved";
            case BASED_ON_APPLIED_JOBS -> "Similar to jobs you applied to";
            case BASED_ON_SEARCH_KEYWORD -> "Matches your recent search keywords";
            case USERS_LIKE_YOU_APPLIED -> "Similar candidates applied to this job";
            case USERS_LIKE_YOU_SAVED -> "Similar candidates saved this job";
        };
    }

    private String buildReasonText(ScoredJob scoredJob, Set<RecommendationReasonCode> reasonCodes) {
        int matchedSkillCount = scoredJob.contentResult().matchedSkills().size();
        List<String> parts = new ArrayList<>();
        if (matchedSkillCount > 0) {
            parts.add("you match " + matchedSkillCount + " required skill(s)");
        }
        if (reasonCodes.contains(RecommendationReasonCode.BASED_ON_SEARCH_KEYWORD)
                || reasonCodes.contains(RecommendationReasonCode.BASED_ON_CLICK_HISTORY)
                || reasonCodes.contains(RecommendationReasonCode.BASED_ON_SAVED_JOBS)) {
            parts.add("your recent behavior points to similar jobs");
        }
        if (reasonCodes.contains(RecommendationReasonCode.USERS_LIKE_YOU_APPLIED)
                || reasonCodes.contains(RecommendationReasonCode.USERS_LIKE_YOU_SAVED)) {
            parts.add("similar candidates engaged with this job");
        }
        if (parts.isEmpty()) {
            return "Recommended from your current job preference.";
        }
        return "Recommended because " + String.join(", and ", parts) + ".";
    }

    private double normalizeRaw(double raw, double maxRaw) {
        if (raw <= 0 || maxRaw <= 0) {
            return 0;
        }
        return roundOneDecimal(Math.min(100.0, (raw / maxRaw) * 100));
    }

    private double jaccard(Set<Long> left, Set<Long> right) {
        if (left == null || right == null || left.isEmpty() || right.isEmpty()) {
            return 0.0;
        }
        Set<Long> intersection = new HashSet<>(left);
        intersection.retainAll(right);
        Set<Long> union = new HashSet<>(left);
        union.addAll(right);
        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
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
        String normalized = normalize(value);
        if (normalized.isBlank()) {
            return Set.of();
        }
        String[] parts = NON_ALPHANUMERIC.split(normalized);
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

    private record ContentResult(double score,
                                 List<String> matchedSkills,
                                 List<String> missingSkills,
                                 Set<RecommendationReasonCode> reasonCodes,
                                 List<String> legacyReasons) {
    }

    private record RawScore(double score, Set<RecommendationReasonCode> reasons) {
    }

    private record SimilarUser(String userId, double similarity) {
    }

    private record CollaborativeProfile(Map<Long, JobCollaborativeStats> jobStats) {
    }

    private static class InterestProfile {
        private final Map<String, Double> skillInterest = new HashMap<>();
        private final Map<String, Double> titleKeywordInterest = new HashMap<>();
        private final Map<String, Double> locationInterest = new HashMap<>();
        private final Map<Long, Double> companyInterest = new HashMap<>();
        private double totalWeight;

        Map<String, Double> skillInterest() {
            return skillInterest;
        }

        Map<String, Double> titleKeywordInterest() {
            return titleKeywordInterest;
        }

        Map<String, Double> locationInterest() {
            return locationInterest;
        }

        Map<Long, Double> companyInterest() {
            return companyInterest;
        }

        double totalWeight() {
            return totalWeight;
        }

        void addTotalWeight(double weight) {
            totalWeight += weight;
        }
    }

    private static class JobCollaborativeStats {
        private double rawScore;
        private double appliedWeight;
        private double savedWeight;

        double rawScore() {
            return rawScore;
        }

        double appliedWeight() {
            return appliedWeight;
        }

        double savedWeight() {
            return savedWeight;
        }

        void addRawScore(double value) {
            rawScore += value;
        }

        void addAppliedWeight(double value) {
            appliedWeight += value;
        }

        void addSavedWeight(double value) {
            savedWeight += value;
        }
    }

    private static class ScoredJob {
        private final Job job;
        private final ContentResult contentResult;
        private final RawScore behaviorRaw;
        private final RawScore collaborativeRaw;
        private Double behaviorScore = 0.0;
        private Double collaborativeScore = 0.0;
        private Double hybridScore = 0.0;

        ScoredJob(Job job, ContentResult contentResult, RawScore behaviorRaw, RawScore collaborativeRaw) {
            this.job = job;
            this.contentResult = contentResult;
            this.behaviorRaw = behaviorRaw;
            this.collaborativeRaw = collaborativeRaw;
        }

        Job job() {
            return job;
        }

        ContentResult contentResult() {
            return contentResult;
        }

        RawScore behaviorRaw() {
            return behaviorRaw;
        }

        RawScore collaborativeRaw() {
            return collaborativeRaw;
        }

        Double getBehaviorScore() {
            return behaviorScore;
        }

        void setBehaviorScore(Double behaviorScore) {
            this.behaviorScore = behaviorScore;
        }

        Double getCollaborativeScore() {
            return collaborativeScore;
        }

        void setCollaborativeScore(Double collaborativeScore) {
            this.collaborativeScore = collaborativeScore;
        }

        Double getHybridScore() {
            return hybridScore;
        }

        void setHybridScore(Double hybridScore) {
            this.hybridScore = hybridScore;
        }
    }
}
