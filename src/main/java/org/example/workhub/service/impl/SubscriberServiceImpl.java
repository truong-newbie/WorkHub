package org.example.workhub.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.workhub.constant.ErrorMessage;
import org.example.workhub.constant.RoleConstant;
import org.example.workhub.constant.SubscriberJobNotificationStatus;
import org.example.workhub.domain.dto.pagination.PaginationResponseDto;
import org.example.workhub.domain.dto.pagination.PagingMeta;
import org.example.workhub.domain.dto.request.SubscriberCreateRequest;
import org.example.workhub.domain.dto.request.SubscriberSearchRequest;
import org.example.workhub.domain.dto.request.SubscriberUpdateRequest;
import org.example.workhub.domain.dto.response.SubscriberMailResponse;
import org.example.workhub.domain.dto.response.SubscriberResponse;
import org.example.workhub.domain.dto.response.SubscriberUnsubscribeResponse;
import org.example.workhub.domain.entity.Job;
import org.example.workhub.domain.entity.Skill;
import org.example.workhub.domain.entity.Subscriber;
import org.example.workhub.domain.entity.User;
import org.example.workhub.domain.mapper.SubscriberMapper;
import org.example.workhub.domain.specification.SubscriberSpecification;
import org.example.workhub.exception.BadRequestException;
import org.example.workhub.exception.ConflictException;
import org.example.workhub.exception.ForbiddenException;
import org.example.workhub.exception.NotFoundException;
import org.example.workhub.repository.JobRepository;
import org.example.workhub.repository.SkillRepository;
import org.example.workhub.repository.SubscriberRepository;
import org.example.workhub.repository.UserRepository;
import org.example.workhub.security.UserPrincipal;
import org.example.workhub.service.EmailQueueService;
import org.example.workhub.service.SubscriberService;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SubscriberServiceImpl implements SubscriberService {

    private static final Set<String> SORT_FIELDS = Set.of("id", "email", "enabled", "subscribedAt", "lastEmailSentAt", "createdDate", "lastModifiedDate");

    private final SubscriberRepository subscriberRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final JobRepository jobRepository;
    private final SubscriberMapper subscriberMapper;
    private final EmailQueueService emailQueueService;
    private final MessageSource messageSource;
    private final SpringTemplateEngine templateEngine;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.backend-url:http://localhost:8080/api/v1}")
    private String backendUrl;

    @Override
    public SubscriberResponse createSubscriber(SubscriberCreateRequest request) {
        UserPrincipal currentUser = getCurrentUserPrincipal();
        User user = getUserFromPrincipal(currentUser);
        validateSkillIds(request.getSkillIds());
        validateDuplicateEmail(request.getEmail(), null);

        subscriberRepository.findByUserIdAndDeletedFalse(user.getId())
                .ifPresent(existing -> {
                    throw new ConflictException(ErrorMessage.Subscriber.ERR_EMAIL_EXISTS);
                });

        Subscriber subscriber = new Subscriber();
        subscriber.setName(resolveName(request.getName(), user));
        subscriber.setEmail(normalizeEmail(request.getEmail()));
        subscriber.setEnabled(request.getEnabled() == null || Boolean.TRUE.equals(request.getEnabled()));
        subscriber.setDeleted(false);
        subscriber.setSubscribedAt(LocalDateTime.now());
        subscriber.setUnsubscribeToken(generateUnsubscribeToken());
        subscriber.setUser(user);
        subscriber.setSkills(resolveSkills(request.getSkillIds()));

        return subscriberMapper.toResponse(subscriberRepository.save(subscriber));
    }

    @Override
    public SubscriberResponse updateSubscriber(Long id, SubscriberUpdateRequest request) {
        UserPrincipal currentUser = getCurrentUserPrincipal();
        Subscriber subscriber = findSubscriber(id);
        validateManagePermission(subscriber, currentUser);

        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            validateDuplicateEmail(request.getEmail(), subscriber.getId());
            subscriber.setEmail(normalizeEmail(request.getEmail()));
        }
        if (request.getName() != null) {
            subscriber.setName(request.getName().trim());
        }
        if (request.getEnabled() != null) {
            subscriber.setEnabled(request.getEnabled());
            if (Boolean.TRUE.equals(request.getEnabled())) {
                subscriber.setUnsubscribedAt(null);
            }
        }
        if (request.getSkillIds() != null) {
            validateSkillIds(request.getSkillIds());
            subscriber.setSkills(resolveSkills(request.getSkillIds()));
        }

        return subscriberMapper.toResponse(subscriberRepository.save(subscriber));
    }

    @Override
    public SubscriberResponse deleteSubscriber(Long id) {
        UserPrincipal currentUser = getCurrentUserPrincipal();
        Subscriber subscriber = findSubscriber(id);
        validateManagePermission(subscriber, currentUser);

        subscriber.setDeleted(true);
        subscriber.setEnabled(false);
        return subscriberMapper.toResponse(subscriberRepository.save(subscriber));
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriberResponse getSubscriberDetail(Long id) {
        UserPrincipal currentUser = getCurrentUserPrincipal();
        Subscriber subscriber = findSubscriber(id);
        validateReadPermission(subscriber, currentUser);
        return subscriberMapper.toResponse(subscriber);
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriberResponse getCurrentUserSubscriber() {
        UserPrincipal currentUser = getCurrentUserPrincipal();
        Subscriber subscriber = subscriberRepository.findByUserIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Subscriber.ERR_NOT_FOUND));
        return subscriberMapper.toResponse(subscriber);
    }

    @Override
    public SubscriberResponse enableSubscriber(Long id) {
        UserPrincipal currentUser = getCurrentUserPrincipal();
        Subscriber subscriber = findSubscriber(id);
        validateManagePermission(subscriber, currentUser);
        subscriber.setEnabled(true);
        subscriber.setUnsubscribedAt(null);
        return subscriberMapper.toResponse(subscriberRepository.save(subscriber));
    }

    @Override
    public SubscriberResponse disableSubscriber(Long id) {
        UserPrincipal currentUser = getCurrentUserPrincipal();
        Subscriber subscriber = findSubscriber(id);
        validateManagePermission(subscriber, currentUser);
        subscriber.setEnabled(false);
        return subscriberMapper.toResponse(subscriberRepository.save(subscriber));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponseDto<SubscriberResponse> getSubscribers(SubscriberSearchRequest request) {
        validateRecruiterOrAdmin(getCurrentUserPrincipal());
        Pageable pageable = buildPageable(request);
        Page<Subscriber> page = subscriberRepository.findAll(SubscriberSpecification.withFilters(request), pageable);
        PagingMeta pagingMeta = new PagingMeta(
                page.getTotalElements(),
                page.getTotalPages(),
                request.getPage() == null ? 1 : request.getPage() + 1,
                request.getSize() == null ? 10 : request.getSize(),
                SORT_FIELDS.contains(request.getSortBy()) ? request.getSortBy() : "subscribedAt",
                request.getSortDir() == null ? "DESC" : request.getSortDir()
        );
        return new PaginationResponseDto<>(pagingMeta, subscriberMapper.toResponses(page.getContent()));
    }

    @Override
    public SubscriberMailResponse sendMatchingJobEmails() {
        List<Subscriber> subscribers = subscriberRepository.findAllEnabledWithSkills();
        int queuedEmails = 0;
        int matchedJobs = 0;
        LocalDateTime now = LocalDateTime.now();

        for (Subscriber subscriber : subscribers) {
            List<Long> skillIds = subscriber.getSkills().stream()
                    .map(Skill::getId)
                    .collect(Collectors.toList());
            if (skillIds.isEmpty()) {
                continue;
            }

            LocalDateTime since = subscriber.getSubscribedAt();
            if (since == null) {
                since = now.minusDays(1);
            }

            List<Job> jobs = jobRepository.findUnsentPublishedJobsBySkillIds(
                    subscriber.getId(),
                    skillIds,
                    since,
                    Instant.now(),
                    List.of(SubscriberJobNotificationStatus.SENT, SubscriberJobNotificationStatus.PENDING)
            );
            if (jobs.isEmpty()) {
                continue;
            }
            ensureUnsubscribeToken(subscriber);

            boolean queued = emailQueueService.enqueueSubscriberMatchingEmail(
                    subscriber,
                    jobs,
                    getMessage("subscriber.mail.subject"),
                    buildMatchingJobEmail(subscriber, jobs),
                    true,
                    now
            );
            if (queued) {
                queuedEmails++;
                matchedJobs += jobs.size();
            }
        }

        return SubscriberMailResponse.builder()
                .checkedSubscribers(subscribers.size())
                .sentEmails(0)
                .queuedEmails(queuedEmails)
                .matchedJobs(matchedJobs)
                .build();
    }

    @Override
    public SubscriberUnsubscribeResponse unsubscribeByToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new NotFoundException(ErrorMessage.Subscriber.ERR_INVALID_UNSUBSCRIBE_TOKEN);
        }

        Subscriber subscriber = subscriberRepository.findByUnsubscribeTokenAndDeletedFalse(token.trim())
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Subscriber.ERR_INVALID_UNSUBSCRIBE_TOKEN));

        LocalDateTime unsubscribedAt = LocalDateTime.now();
        subscriber.setEnabled(false);
        subscriber.setUnsubscribedAt(unsubscribedAt);
        subscriberRepository.save(subscriber);

        return SubscriberUnsubscribeResponse.builder()
                .email(subscriber.getEmail())
                .enabled(false)
                .unsubscribedAt(unsubscribedAt)
                .message(getMessage("subscriber.unsubscribe.success"))
                .build();
    }

    private String buildMatchingJobEmail(Subscriber subscriber, List<Job> jobs) {
        String subscriberName = subscriber.getName() != null && !subscriber.getName().isBlank()
                ? subscriber.getName()
                : subscriber.getEmail();
        Context context = new Context(LocaleContextHolder.getLocale());
        context.setVariable("subscriberName", subscriberName);
        context.setVariable("header", getMessage("subscriber.mail.body.header", subscriberName));
        context.setVariable("footer", getMessage("subscriber.mail.body.footer"));
        context.setVariable("jobs", jobs.stream().map(this::toEmailJobItem).collect(Collectors.toList()));
        context.setVariable("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        context.setVariable("unsubscribeUrl", buildUnsubscribeUrl(subscriber));
        return templateEngine.process("email/subscriber-job-matching", context);
    }

    private EmailJobItem toEmailJobItem(Job job) {
        String companyName = job.getCompany() != null ? job.getCompany().getName() : "WorkHub company";
        String skills = job.getSkills() == null ? "" : job.getSkills().stream()
                .map(Skill::getName)
                .collect(Collectors.joining(", "));
        return EmailJobItem.builder()
                .id(job.getId())
                .title(job.getTitle())
                .companyName(companyName)
                .location(job.getLocation())
                .salary(buildSalary(job))
                .employmentType(job.getEmploymentType())
                .level(job.getLevel() != null ? job.getLevel().name() : null)
                .skills(skills)
                .url(frontendUrl + "/jobs/" + job.getId())
                .build();
    }

    private String buildSalary(Job job) {
        if (Boolean.TRUE.equals(job.getNegotiableSalary())) {
            return "Negotiable";
        }
        if (job.getSalaryMin() == null && job.getSalaryMax() == null) {
            return "Not disclosed";
        }
        if (job.getSalaryMin() != null && job.getSalaryMax() != null) {
            return job.getSalaryMin() + " - " + job.getSalaryMax();
        }
        return job.getSalaryMin() != null ? "From " + job.getSalaryMin() : "Up to " + job.getSalaryMax();
    }

    private void ensureUnsubscribeToken(Subscriber subscriber) {
        if (subscriber.getUnsubscribeToken() != null && !subscriber.getUnsubscribeToken().isBlank()) {
            return;
        }
        subscriber.setUnsubscribeToken(generateUnsubscribeToken());
        subscriberRepository.save(subscriber);
    }

    private String generateUnsubscribeToken() {
        return UUID.randomUUID().toString();
    }

    private String buildUnsubscribeUrl(Subscriber subscriber) {
        return backendUrl + "/subscribers/unsubscribe?token=" + subscriber.getUnsubscribeToken();
    }

    @lombok.Getter
    @lombok.Builder
    private static class EmailJobItem {
        private Long id;
        private String title;
        private String companyName;
        private String location;
        private String salary;
        private String employmentType;
        private String level;
        private String skills;
        private String url;
    }

    private Subscriber findSubscriber(Long id) {
        return subscriberRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Subscriber.ERR_NOT_FOUND));
    }

    private void validateSkillIds(List<Long> skillIds) {
        if (skillIds == null || skillIds.isEmpty()) {
            throw new BadRequestException(ErrorMessage.Subscriber.ERR_SKILL_EMPTY);
        }
    }

    private List<Skill> resolveSkills(List<Long> skillIds) {
        List<Skill> skills = skillRepository.findAllById(skillIds);
        if (skills.size() != skillIds.stream().distinct().count()) {
            throw new NotFoundException(ErrorMessage.Skill.ERR_NOT_FOUND);
        }
        return skills;
    }

    private void validateDuplicateEmail(String email, Long currentId) {
        String normalizedEmail = normalizeEmail(email);
        if (currentId == null) {
            if (subscriberRepository.existsByEmailIgnoreCaseAndDeletedFalse(normalizedEmail)) {
                throw new ConflictException(ErrorMessage.Subscriber.ERR_EMAIL_EXISTS);
            }
            return;
        }
        if (subscriberRepository.existsByEmailIgnoreCaseAndDeletedFalseAndIdNot(normalizedEmail, currentId)) {
            throw new ConflictException(ErrorMessage.Subscriber.ERR_EMAIL_EXISTS);
        }
    }

    private void validateReadPermission(Subscriber subscriber, UserPrincipal currentUser) {
        if (isAdmin(currentUser) || hasRole(currentUser, RoleConstant.RECRUITER) || isOwner(subscriber, currentUser)) {
            return;
        }
        throw new ForbiddenException(ErrorMessage.Subscriber.ERR_PERMISSION_DENIED);
    }

    private void validateManagePermission(Subscriber subscriber, UserPrincipal currentUser) {
        if (isAdmin(currentUser) || isOwner(subscriber, currentUser)) {
            return;
        }
        throw new ForbiddenException(ErrorMessage.Subscriber.ERR_PERMISSION_DENIED);
    }

    private void validateRecruiterOrAdmin(UserPrincipal currentUser) {
        if (isAdmin(currentUser) || hasRole(currentUser, RoleConstant.RECRUITER)) {
            return;
        }
        throw new ForbiddenException(ErrorMessage.Subscriber.ERR_PERMISSION_DENIED);
    }

    private boolean isOwner(Subscriber subscriber, UserPrincipal currentUser) {
        return subscriber.getUser() != null && currentUser.getId().equals(subscriber.getUser().getId());
    }

    private boolean isAdmin(UserPrincipal currentUser) {
        return hasRole(currentUser, RoleConstant.ADMIN);
    }

    private boolean hasRole(UserPrincipal currentUser, String role) {
        return currentUser.getAuthorities() != null && currentUser.getAuthorities().stream()
                .anyMatch(authority -> role.equals(authority.getAuthority()));
    }

    private UserPrincipal getCurrentUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            throw new ForbiddenException(ErrorMessage.UNAUTHORIZED);
        }
        return (UserPrincipal) authentication.getPrincipal();
    }

    private User getUserFromPrincipal(UserPrincipal principal) {
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{principal.getId()}));
    }

    private Pageable buildPageable(SubscriberSearchRequest request) {
        int page = request.getPage() == null || request.getPage() < 0 ? 0 : request.getPage();
        int size = request.getSize() == null || request.getSize() < 1 ? 10 : Math.min(request.getSize(), 100);
        String sortBy = SORT_FIELDS.contains(request.getSortBy()) ? request.getSortBy() : "subscribedAt";
        Sort sort = "ASC".equalsIgnoreCase(request.getSortDir()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        return PageRequest.of(page, size, sort);
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private String resolveName(String requestedName, User user) {
        if (requestedName != null && !requestedName.trim().isEmpty()) {
            return requestedName.trim();
        }
        return user.getUsername();
    }

    private String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }
}
