package org.example.workhub.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.workhub.config.RecommendationProperties;
import org.example.workhub.constant.ErrorMessage;
import org.example.workhub.domain.dto.request.JobBehaviorTrackRequest;
import org.example.workhub.domain.dto.request.JobSearchTrackRequest;
import org.example.workhub.domain.dto.response.JobBehaviorSummaryResponse;
import org.example.workhub.domain.entity.FavoriteJob;
import org.example.workhub.domain.entity.Job;
import org.example.workhub.domain.entity.JobApplication;
import org.example.workhub.domain.entity.JobClickHistory;
import org.example.workhub.domain.entity.JobSearchHistory;
import org.example.workhub.domain.entity.JobViewHistory;
import org.example.workhub.domain.entity.Skill;
import org.example.workhub.domain.entity.User;
import org.example.workhub.exception.BadRequestException;
import org.example.workhub.exception.NotFoundException;
import org.example.workhub.repository.FavoriteJobRepository;
import org.example.workhub.repository.JobApplicationRepository;
import org.example.workhub.repository.JobClickHistoryRepository;
import org.example.workhub.repository.JobRepository;
import org.example.workhub.repository.JobSearchHistoryRepository;
import org.example.workhub.repository.JobViewHistoryRepository;
import org.example.workhub.repository.UserRepository;
import org.example.workhub.security.UserPrincipal;
import org.example.workhub.service.JobBehaviorService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class JobBehaviorServiceImpl implements JobBehaviorService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final JobViewHistoryRepository jobViewHistoryRepository;
    private final JobClickHistoryRepository jobClickHistoryRepository;
    private final JobSearchHistoryRepository jobSearchHistoryRepository;
    private final FavoriteJobRepository favoriteJobRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final RecommendationProperties recommendationProperties;

    @Override
    public void trackView(Long jobId, JobBehaviorTrackRequest request) {
        User user = getCurrentUser();
        Job job = getAvailableJob(jobId);

        JobViewHistory history = new JobViewHistory();
        history.setUser(user);
        history.setJob(job);
        history.setSource(request == null ? null : request.getSource());
        history.setSessionId(request == null ? null : request.getSessionId());
        history.setViewedAt(LocalDateTime.now());
        jobViewHistoryRepository.save(history);
    }

    @Override
    public void trackClick(Long jobId, JobBehaviorTrackRequest request) {
        User user = getCurrentUser();
        Job job = getAvailableJob(jobId);

        JobClickHistory history = new JobClickHistory();
        history.setUser(user);
        history.setJob(job);
        history.setSource(request == null ? null : request.getSource());
        history.setPosition(request == null ? null : request.getPosition());
        history.setClickedAt(LocalDateTime.now());
        jobClickHistoryRepository.save(history);
    }

    @Override
    public void trackSearch(JobSearchTrackRequest request) {
        User user = getCurrentUser();

        JobSearchHistory history = new JobSearchHistory();
        history.setUser(user);
        history.setKeyword(request.getKeyword().trim());
        history.setFiltersJson(request.getFiltersJson());
        history.setSearchedAt(LocalDateTime.now());
        jobSearchHistoryRepository.save(history);
    }

    @Override
    @Transactional(readOnly = true)
    public JobBehaviorSummaryResponse getBehaviorSummary() {
        UserPrincipal principal = getCurrentUserPrincipal();
        LocalDateTime since = LocalDateTime.now().minusDays(recommendationProperties.getBehavior().getHistoryDays());
        Instant sinceInstant = since.atZone(ZoneId.systemDefault()).toInstant();

        List<JobViewHistory> views = jobViewHistoryRepository.findByUserIdSince(principal.getId(), since);
        List<JobClickHistory> clicks = jobClickHistoryRepository.findByUserIdSince(principal.getId(), since);
        List<FavoriteJob> favorites = favoriteJobRepository.findActiveByUserIdSince(principal.getId(), sinceInstant);
        List<JobApplication> applications = jobApplicationRepository.findActiveByUserIdSince(principal.getId(), sinceInstant);
        List<JobSearchHistory> searches = jobSearchHistoryRepository.findByUserIdSince(principal.getId(), since);

        Map<String, Double> skills = new HashMap<>();
        Map<String, Double> titleKeywords = new HashMap<>();
        Map<String, Double> locations = new HashMap<>();

        views.forEach(history -> addJobInterest(history.getJob(), 1.0, skills, titleKeywords, locations));
        clicks.forEach(history -> addJobInterest(history.getJob(), 3.0, skills, titleKeywords, locations));
        favorites.forEach(favorite -> addJobInterest(favorite.getJob(), 5.0, skills, titleKeywords, locations));
        applications.forEach(application -> addJobInterest(application.getJob(), 10.0, skills, titleKeywords, locations));
        searches.forEach(search -> addKeywordInterest(search.getKeyword(), 2.0, titleKeywords));

        return JobBehaviorSummaryResponse.builder()
                .totalViewed(views.size())
                .totalClicked(clicks.size())
                .totalSaved(favorites.size())
                .totalApplied(applications.size())
                .topSkillInterests(topKeys(skills))
                .topTitleKeywords(topKeys(titleKeywords))
                .topLocations(topKeys(locations))
                .build();
    }

    private Job getAvailableJob(Long jobId) {
        return jobRepository.findAvailablePublishedJobById(jobId, Instant.now())
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Recommendation.ERR_JOB_NOT_FOUND, new String[]{String.valueOf(jobId)}));
    }

    private User getCurrentUser() {
        UserPrincipal principal = getCurrentUserPrincipal();
        return userRepository.findById(principal.getId())
                .filter(user -> !Boolean.TRUE.equals(user.getDeleted()))
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{principal.getId()}));
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

    private void addJobInterest(Job job,
                                double weight,
                                Map<String, Double> skills,
                                Map<String, Double> titleKeywords,
                                Map<String, Double> locations) {
        if (job == null) {
            return;
        }
        if (job.getSkills() != null) {
            for (Skill skill : job.getSkills()) {
                addWeight(skills, skill.getName(), weight);
            }
        }
        addKeywordInterest(job.getTitle(), weight, titleKeywords);
        addWeight(locations, job.getLocation(), weight);
    }

    private void addKeywordInterest(String value, double weight, Map<String, Double> titleKeywords) {
        if (value == null || value.isBlank()) {
            return;
        }
        for (String token : value.toLowerCase(Locale.ROOT).split("[^a-z0-9]+")) {
            if (token.length() >= 2) {
                addWeight(titleKeywords, token, weight);
            }
        }
    }

    private void addWeight(Map<String, Double> map, String key, double weight) {
        if (key == null || key.isBlank()) {
            return;
        }
        map.merge(key.trim(), weight, Double::sum);
    }

    private List<String> topKeys(Map<String, Double> source) {
        return source.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue(Comparator.reverseOrder()))
                .limit(10)
                .map(Map.Entry::getKey)
                .toList();
    }
}
