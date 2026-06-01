package org.example.workhub.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.workhub.constant.*;
import org.example.workhub.domain.dto.internal.*;
import org.example.workhub.domain.dto.pagination.PaginationResponseDto;
import org.example.workhub.domain.dto.pagination.PagingMeta;
import org.example.workhub.domain.dto.request.ChatMessageRequest;
import org.example.workhub.domain.dto.request.CompanySearchRequest;
import org.example.workhub.domain.dto.request.JobSearchRequest;
import org.example.workhub.domain.dto.request.ResumeSearchRequest;
import org.example.workhub.domain.dto.response.*;
import org.example.workhub.domain.entity.*;
import org.example.workhub.domain.mapper.ChatMapper;
import org.example.workhub.exception.ForbiddenException;
import org.example.workhub.exception.InternalServerException;
import org.example.workhub.exception.NotFoundException;
import org.example.workhub.exception.BadRequestException;
import org.example.workhub.repository.ChatConversationRepository;
import org.example.workhub.repository.ChatMessageRepository;
import org.example.workhub.repository.JobRepository;
import org.example.workhub.repository.UserRepository;
import org.example.workhub.security.UserPrincipal;
import org.example.workhub.service.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ChatServiceImpl implements ChatService {

    private static final int MAX_ASSISTANT_LENGTH = 5000;
    private static final int MAX_FILTER_LENGTH = 120;
    private static final int MAX_SKILLS = 10;
    private static final String REFUSAL_KEY = "chat.refusal";
    private static final String UNAVAILABLE_KEY = "chat.unavailable";

    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final JobSearchService jobSearchService;
    private final JobRecommendationService recommendationService;
    private final FavoriteJobService favoriteJobService;
    private final JobApplicationService applicationService;
    private final ResumeService resumeService;
    private final CompanyService companyService;
    private final AiChatClient aiChatClient;
    private final ChatRateLimitService rateLimitService;
    private final ChatMapper chatMapper;
    private final MessageSource messageSource;

    @Value("${chat.enabled:true}")
    private boolean chatEnabled;

    @Value("${chat.max-context-items:5}")
    private int maxContextItems;

    @Value("${chat.max-history-messages:10}")
    private int maxHistoryMessages;

    @Value("${chat.max-message-length:1000}")
    private int maxMessageLength;

    @Override
    public ChatMessageResponse sendMessage(ChatMessageRequest request) {
        User candidate = getCurrentCandidate();
        ensureEnabled();
        rateLimitService.checkAllowed(candidate.getId());

        String userText = request.getMessage().trim();
        if (userText.length() > messageLimit()) {
            throw new BadRequestException(ErrorMessage.Chat.ERR_MESSAGE_TOO_LONG);
        }
        ChatConversation conversation = resolveConversation(request.getConversationId(), candidate, userText);
        List<ChatAiHistoryItem> history = recentHistory(conversation.getId());
        saveMessage(conversation, ChatSenderType.USER, userText, null, null, false, List.of(), List.of());

        ChatIntent intent;
        ChatResponseMode mode;
        boolean outOfScope;
        String answer;
        ContextResult context = ContextResult.empty();

        if (looksUnsafe(userText)) {
            intent = ChatIntent.OUT_OF_SCOPE;
            mode = ChatResponseMode.REFUSAL;
            outOfScope = true;
            answer = message(REFUSAL_KEY);
        } else {
            ChatAiIntentResponse classified = classifySafely(userText, history);
            intent = ChatIntent.fromExternalValue(classified == null ? null : classified.getIntent());
            outOfScope = intent == ChatIntent.OUT_OF_SCOPE || classified == null || Boolean.TRUE.equals(classified.getOutOfScope());
            if (outOfScope) {
                mode = classified == null ? ChatResponseMode.FALLBACK : ChatResponseMode.REFUSAL;
                answer = classified == null ? message(UNAVAILABLE_KEY) : message(REFUSAL_KEY);
            } else {
                context = loadContext(intent, sanitize(classified));
                GeneratedAnswer generated = answerSafely(userText, intent, history, context);
                answer = generated.answer();
                mode = generated.mode();
            }
        }

        ChatMessage assistant = saveMessage(
                conversation,
                ChatSenderType.ASSISTANT,
                trim(answer, MAX_ASSISTANT_LENGTH),
                intent,
                mode,
                outOfScope,
                context.sources(),
                context.actions()
        );
        conversation.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conversation);
        log.info("Chat response candidateId={} conversationId={} intent={} mode={} itemCount={}",
                candidate.getId(), conversation.getId(), intent, mode, context.contextItems().size());

        return ChatMessageResponse.builder()
                .conversationId(conversation.getId())
                .answer(assistant.getContent())
                .intent(intent)
                .outOfScope(outOfScope)
                .responseMode(mode)
                .sources(context.sources())
                .suggestedActions(context.actions())
                .createdAt(assistant.getCreatedDate())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponseDto<ChatConversationResponse> getMyConversations(int page, int size) {
        User candidate = getCurrentCandidate();
        Page<ChatConversation> result = conversationRepository.findByCandidateIdAndDeletedFalse(
                candidate.getId(),
                PageRequest.of(normalizePage(page), clamp(size, 1, 50), Sort.by("lastMessageAt").descending())
        );
        return new PaginationResponseDto<>(
                meta(result, page, clamp(size, 1, 50), "lastMessageAt", "DESC"),
                result.getContent().stream().map(chatMapper::toConversationResponse).toList()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponseDto<ChatHistoryMessageResponse> getMessages(Long conversationId, int page, int size) {
        User candidate = getCurrentCandidate();
        findOwnedConversation(conversationId, candidate.getId());
        int safeSize = clamp(size, 1, 100);
        Page<ChatMessage> result = messageRepository.findByConversationId(
                conversationId,
                PageRequest.of(normalizePage(page), safeSize, Sort.by("createdDate").ascending().and(Sort.by("id").ascending()))
        );
        return new PaginationResponseDto<>(
                meta(result, page, safeSize, "createdDate", "ASC"),
                result.getContent().stream().map(chatMapper::toHistoryResponse).toList()
        );
    }

    @Override
    public void deleteConversation(Long conversationId) {
        User candidate = getCurrentCandidate();
        ChatConversation conversation = findOwnedConversation(conversationId, candidate.getId());
        conversation.setDeleted(true);
        conversationRepository.save(conversation);
    }

    private ContextResult loadContext(ChatIntent intent, ChatAiIntentResponse classified) {
        return switch (intent) {
            case SEARCH_JOBS -> searchJobs(classified);
            case RECOMMEND_JOBS -> recommendedJobs();
            case JOB_DETAIL -> jobDetail(classified.getJobId());
            case SAVED_JOBS -> savedJobs();
            case APPLICATION_STATUS -> applications();
            case MY_RESUMES -> resumes();
            case COMPANY_INFO -> companyInfo(classified);
            case PLATFORM_HELP -> platformHelp();
            case OUT_OF_SCOPE -> ContextResult.empty();
        };
    }

    private ContextResult searchJobs(ChatAiIntentResponse classified) {
        JobSearchRequest request = new JobSearchRequest();
        request.setKeyword(classified.getKeyword());
        request.setLocation(classified.getLocation());
        request.setSkillNames(classified.getSkillNames());
        request.setLevel(classified.getLevel());
        request.setEmploymentType(classified.getEmploymentType());
        request.setSalaryMin(classified.getSalaryMin());
        request.setSalaryMax(classified.getSalaryMax());
        request.setPageNum(1);
        request.setPageSize(contextLimit());
        List<JobSearchResponse> jobs = items(jobSearchService.search(request));
        return fromSearchJobs(jobs);
    }

    private ContextResult recommendedJobs() {
        List<RecommendedJobResponse> jobs = items(recommendationService.getRecommendedJobs(
                PageRequest.of(0, contextLimit()), null, false, true
        ));
        List<Map<String, Object>> context = new ArrayList<>();
        List<ChatSourceResponse> sources = new ArrayList<>();
        for (RecommendedJobResponse job : jobs) {
            Long id = job.getJobId() != null ? job.getJobId() : job.getId();
            context.add(mapOf(
                    "type", "JOB", "id", string(id), "title", job.getTitle(),
                    "companyName", job.getCompanyName(), "location", job.getLocation(),
                    "skills", job.getSkillNames(), "matchScore", job.getMatchScore(),
                    "matchReasons", job.getMatchReasons()
            ));
            sources.add(jobSource(id, job.getTitle(), subtitle(job.getCompanyName(), job.getLocation())));
        }
        return new ContextResult(context, sources, List.of(action(ChatActionType.VIEW_RECOMMENDATIONS, "View recommendations", "/candidate/jobs/recommended")));
    }

    private ContextResult jobDetail(Long jobId) {
        if (jobId == null) {
            return ContextResult.empty();
        }
        Job job = jobRepository.findAvailablePublishedJobById(jobId, Instant.now()).orElse(null);
        if (job == null || job.getCompany() == null || !Boolean.TRUE.equals(job.getCompany().getVerified())) {
            return ContextResult.empty();
        }
        Map<String, Object> item = jobContext(job);
        return new ContextResult(List.of(item), List.of(jobSource(job.getId(), job.getTitle(), subtitle(job.getCompany().getName(), job.getLocation()))),
                List.of(action(ChatActionType.VIEW_JOB, "View job detail", "/jobs/" + job.getId())));
    }

    private ContextResult savedJobs() {
        List<FavoriteJobResponse> favorites = items(favoriteJobService.getMyFavorites(0, contextLimit()));
        List<Map<String, Object>> context = new ArrayList<>();
        List<ChatSourceResponse> sources = new ArrayList<>();
        for (FavoriteJobResponse favorite : favorites) {
            JobResponse job = favorite.getJob();
            if (job == null) continue;
            context.add(jobContext(job, "SAVED_JOB"));
            sources.add(jobSource(job.getId(), job.getTitle(), subtitle(companyName(job), job.getLocation())));
        }
        return new ContextResult(context, sources, List.of(action(ChatActionType.VIEW_SAVED_JOBS, "View saved jobs", "/jobs/favorites")));
    }

    private ContextResult applications() {
        List<JobApplicationResponse> applications = items(applicationService.getMyApplications(0, contextLimit()));
        List<Map<String, Object>> context = new ArrayList<>();
        List<ChatSourceResponse> sources = new ArrayList<>();
        for (JobApplicationResponse application : applications) {
            JobApplicationResponse.JobBasicInfo job = application.getJob();
            context.add(mapOf(
                    "type", "APPLICATION", "id", string(application.getId()),
                    "status", application.getStatus(), "appliedAt", application.getAppliedAt(),
                    "jobId", job == null ? null : string(job.getId()),
                    "jobTitle", job == null ? null : job.getTitle(),
                    "companyName", job == null ? null : job.getCompanyName()
            ));
            sources.add(source(ChatSourceType.APPLICATION, application.getId(), job == null ? "Application" : job.getTitle(),
                    application.getStatus() == null ? null : application.getStatus().name(), "/applications/me"));
        }
        return new ContextResult(context, sources, List.of(action(ChatActionType.VIEW_APPLICATIONS, "View applications", "/applications/me")));
    }

    private ContextResult resumes() {
        ResumeSearchRequest request = ResumeSearchRequest.builder().page(0).size(contextLimit()).build();
        List<ResumeResponse> resumes = items(resumeService.getMyResumes(request));
        List<Map<String, Object>> context = new ArrayList<>();
        List<ChatSourceResponse> sources = new ArrayList<>();
        for (ResumeResponse resume : resumes) {
            context.add(mapOf(
                    "type", "RESUME", "id", string(resume.getId()), "title", resume.getTitle(),
                    "fileType", resume.getFileType(), "isDefault", resume.getIsDefault(),
                    "isPublic", resume.getIsPublic(), "uploadedAt", resume.getUploadedAt(),
                    "skills", resume.getSkills() == null ? List.of() : resume.getSkills().stream().map(ResumeResponse.SkillInfo::getName).toList()
            ));
            sources.add(source(ChatSourceType.RESUME, resume.getId(), resume.getTitle(), resume.getFileType(), "/resumes/" + resume.getId()));
        }
        return new ContextResult(context, sources, List.of(action(ChatActionType.VIEW_RESUMES, "View resumes", "/resumes")));
    }

    private ContextResult companyInfo(ChatAiIntentResponse classified) {
        CompanyResponseDto company = null;
        if (classified.getCompanyId() != null) {
            try {
                company = companyService.getById(classified.getCompanyId());
            } catch (RuntimeException ignored) {
                return ContextResult.empty();
            }
        } else if (classified.getCompanyName() != null) {
            CompanySearchRequest request = new CompanySearchRequest();
            request.setName(classified.getCompanyName());
            request.setActive(true);
            request.setVerified(true);
            request.setPageNum(1);
            request.setPageSize(1);
            company = first(items(companyService.getAll(request)));
        }
        if (company == null || !Boolean.TRUE.equals(company.getActive()) || !Boolean.TRUE.equals(company.getVerified())) {
            return ContextResult.empty();
        }
        List<Map<String, Object>> context = new ArrayList<>();
        List<ChatSourceResponse> sources = new ArrayList<>();
        context.add(mapOf(
                "type", "COMPANY", "id", string(company.getId()), "name", company.getName(),
                "city", company.getCity(), "country", company.getCountry(), "industry", company.getIndustry(),
                "companySize", company.getCompanySize(), "description", trim(company.getDescription(), 500)
        ));
        sources.add(source(ChatSourceType.COMPANY, company.getId(), company.getName(), company.getCity(), "/companies/" + company.getId()));
        CompanySearchRequest jobsRequest = new CompanySearchRequest();
        jobsRequest.setPageNum(1);
        jobsRequest.setPageSize(Math.max(1, contextLimit() - 1));
        for (JobResponse job : items(companyService.getCompanyJobs(company.getId(), jobsRequest))) {
            if (context.size() >= contextLimit()) break;
            context.add(jobContext(job, "JOB"));
            sources.add(jobSource(job.getId(), job.getTitle(), subtitle(company.getName(), job.getLocation())));
        }
        return new ContextResult(context, sources, List.of(action(ChatActionType.VIEW_COMPANY, "View company", "/companies/" + company.getId())));
    }

    private ContextResult platformHelp() {
        Map<String, Object> help = mapOf(
                "type", "HELP", "title", "Candidate features",
                "topics", List.of(
                        "Search jobs and open job detail pages",
                        "Save or remove jobs from saved jobs",
                        "Upload resumes and choose a resume when applying",
                        "Withdraw eligible applications and review application statuses",
                        "Configure job preferences and view recommended jobs",
                        "Subscribe to matching-job emails"
                )
        );
        return new ContextResult(List.of(help), List.of(source(ChatSourceType.HELP, "candidate-features", "Candidate features", null, null)),
                List.of(action(ChatActionType.OPEN_JOB_SEARCH, "Search jobs", "/jobs/search")));
    }

    private ContextResult fromSearchJobs(List<JobSearchResponse> jobs) {
        List<Map<String, Object>> context = new ArrayList<>();
        List<ChatSourceResponse> sources = new ArrayList<>();
        for (JobSearchResponse job : jobs) {
            context.add(mapOf(
                    "type", "JOB", "id", string(job.getId()), "title", job.getTitle(),
                    "companyName", job.getCompanyName(), "location", job.getLocation(),
                    "level", job.getLevel(), "employmentType", job.getEmploymentType(),
                    "skills", job.getSkillNames(), "salaryMin", job.getSalaryMin(),
                    "salaryMax", job.getSalaryMax(), "negotiableSalary", job.getNegotiableSalary()
            ));
            sources.add(jobSource(job.getId(), job.getTitle(), subtitle(job.getCompanyName(), job.getLocation())));
        }
        return new ContextResult(context, sources, List.of(action(ChatActionType.OPEN_JOB_SEARCH, "Open job search", "/jobs/search")));
    }

    private GeneratedAnswer answerSafely(String userText, ChatIntent intent, List<ChatAiHistoryItem> history, ContextResult context) {
        if (context.contextItems().isEmpty()) {
            return new GeneratedAnswer(fallbackAnswer(intent, 0), ChatResponseMode.FALLBACK);
        }
        try {
            ChatAiResponse response = aiChatClient.generateResponse(ChatAiResponseRequest.builder()
                    .message(userText)
                    .intent(intent.name())
                    .recentMessages(history)
                    .contextItems(context.contextItems())
                    .build());
            if (response != null && response.getAnswer() != null && !response.getAnswer().isBlank()) {
                return new GeneratedAnswer(trim(response.getAnswer(), MAX_ASSISTANT_LENGTH), ChatResponseMode.AI);
            }
        } catch (RuntimeException ex) {
            log.warn("AI chat answer unavailable intent={} cause={}", intent, ex.getClass().getSimpleName());
        }
        return new GeneratedAnswer(fallbackAnswer(intent, context.contextItems().size()), ChatResponseMode.FALLBACK);
    }

    private ChatAiIntentResponse classifySafely(String userText, List<ChatAiHistoryItem> history) {
        try {
            return aiChatClient.classifyIntent(ChatAiIntentRequest.builder().message(userText).recentMessages(history).build());
        } catch (RuntimeException ex) {
            log.warn("AI chat intent unavailable cause={}", ex.getClass().getSimpleName());
            return null;
        }
    }

    private ChatAiIntentResponse sanitize(ChatAiIntentResponse value) {
        value.setKeyword(trim(value.getKeyword(), MAX_FILTER_LENGTH));
        value.setLocation(trim(value.getLocation(), MAX_FILTER_LENGTH));
        value.setCompanyName(trim(value.getCompanyName(), MAX_FILTER_LENGTH));
        value.setLevel(trim(value.getLevel(), 50));
        value.setEmploymentType(trim(value.getEmploymentType(), 50));
        List<String> skills = value.getSkillNames() == null ? List.of() : value.getSkillNames().stream()
                .filter(Objects::nonNull).map(skill -> trim(skill, 80)).filter(skill -> !skill.isBlank()).limit(MAX_SKILLS).toList();
        value.setSkillNames(skills);
        return value;
    }

    private String fallbackAnswer(ChatIntent intent, int count) {
        String key = switch (intent) {
            case SEARCH_JOBS, RECOMMEND_JOBS, JOB_DETAIL, COMPANY_INFO -> count == 0 ? "chat.no.data" : "chat.fallback.jobs";
            case SAVED_JOBS -> "chat.fallback.saved";
            case APPLICATION_STATUS -> "chat.fallback.applications";
            case MY_RESUMES -> "chat.fallback.resumes";
            case PLATFORM_HELP -> "chat.fallback.help";
            case OUT_OF_SCOPE -> REFUSAL_KEY;
        };
        return message(key, count);
    }

    private List<ChatAiHistoryItem> recentHistory(Long conversationId) {
        List<ChatMessage> messages = messageRepository.findByConversationId(
                conversationId,
                PageRequest.of(0, historyLimit(), Sort.by("createdDate").descending().and(Sort.by("id").descending()))
        ).getContent();
        List<ChatAiHistoryItem> history = new ArrayList<>();
        for (int index = messages.size() - 1; index >= 0; index--) {
            ChatMessage item = messages.get(index);
            history.add(ChatAiHistoryItem.builder().senderType(item.getSenderType().name()).content(trim(item.getContent(), 1000)).build());
        }
        return history;
    }

    private ChatConversation resolveConversation(Long conversationId, User candidate, String userText) {
        if (conversationId != null) {
            return findOwnedConversation(conversationId, candidate.getId());
        }
        ChatConversation conversation = new ChatConversation();
        conversation.setCandidate(candidate);
        conversation.setTitle(trim(userText, 120));
        conversation.setLastMessageAt(LocalDateTime.now());
        conversation.setDeleted(false);
        return conversationRepository.save(conversation);
    }

    private ChatMessage saveMessage(ChatConversation conversation, ChatSenderType senderType, String content,
                                    ChatIntent intent, ChatResponseMode responseMode, boolean outOfScope,
                                    List<ChatSourceResponse> sources, List<ChatActionResponse> actions) {
        ChatMessage message = new ChatMessage();
        message.setConversation(conversation);
        message.setSenderType(senderType);
        message.setContent(content);
        message.setIntent(intent);
        message.setResponseMode(responseMode);
        message.setOutOfScope(outOfScope);
        message.setSourcesJson(chatMapper.writeJson(sources));
        message.setActionsJson(chatMapper.writeJson(actions));
        return messageRepository.save(message);
    }

    private ChatConversation findOwnedConversation(Long conversationId, String candidateId) {
        return conversationRepository.findByIdAndCandidateIdAndDeletedFalse(conversationId, candidateId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Chat.ERR_CONVERSATION_NOT_FOUND, new String[]{String.valueOf(conversationId)}));
    }

    private User getCurrentCandidate() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken
                || !(authentication.getPrincipal() instanceof UserPrincipal principal)
                || principal.getAuthorities() == null
                || principal.getAuthorities().stream().noneMatch(authority -> RoleConstant.CANDIDATE.equals(authority.getAuthority()))) {
            throw new ForbiddenException(ErrorMessage.Chat.ERR_CANDIDATE_ONLY);
        }
        return userRepository.findById(principal.getId())
                .filter(user -> !Boolean.TRUE.equals(user.getDeleted()))
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{principal.getId()}));
    }

    private void ensureEnabled() {
        if (!chatEnabled) {
            throw new InternalServerException(ErrorMessage.Chat.ERR_DISABLED);
        }
    }

    private boolean looksUnsafe(String value) {
        String lower = value.toLowerCase(Locale.ROOT);
        return lower.contains("ignore previous")
                || lower.contains("ignore all previous")
                || lower.contains("system prompt")
                || lower.contains("api key")
                || lower.contains("database password")
                || lower.contains("show all users")
                || lower.contains("drop table")
                || lower.contains("select * from");
    }

    private Map<String, Object> jobContext(Job job) {
        return mapOf(
                "type", "JOB", "id", string(job.getId()), "title", job.getTitle(),
                "companyName", job.getCompany() == null ? null : job.getCompany().getName(),
                "location", job.getLocation(), "level", job.getLevel(), "employmentType", job.getEmploymentType(),
                "skills", job.getSkills() == null ? List.of() : job.getSkills().stream().map(Skill::getName).toList(),
                "salaryMin", job.getSalaryMin(), "salaryMax", job.getSalaryMax(),
                "negotiableSalary", job.getNegotiableSalary(), "summary", trim(job.getDescription(), 500)
        );
    }

    private Map<String, Object> jobContext(JobResponse job, String type) {
        return mapOf(
                "type", type, "id", string(job.getId()), "title", job.getTitle(),
                "companyName", companyName(job), "location", job.getLocation(), "level", job.getLevel(),
                "employmentType", job.getEmploymentType(),
                "skills", job.getSkills() == null ? List.of() : job.getSkills().stream().map(JobResponse.SkillInfo::getName).toList(),
                "salaryMin", job.getSalaryMin(), "salaryMax", job.getSalaryMax(), "negotiableSalary", job.getNegotiableSalary()
        );
    }

    private ChatSourceResponse jobSource(Long id, String title, String subtitle) {
        return source(ChatSourceType.JOB, id, title, subtitle, "/jobs/" + id);
    }

    private ChatSourceResponse source(ChatSourceType type, Object id, String title, String subtitle, String url) {
        return ChatSourceResponse.builder().type(type).id(string(id)).title(title).subtitle(subtitle).url(url).build();
    }

    private ChatActionResponse action(ChatActionType type, String label, String url) {
        return ChatActionResponse.builder().type(type).label(label).url(url).build();
    }

    private String companyName(JobResponse job) {
        return job.getCompany() == null ? null : job.getCompany().getName();
    }

    private String subtitle(String first, String second) {
        if (first == null) return second;
        if (second == null) return first;
        return first + " - " + second;
    }

    private String string(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String trim(String value, int maxLength) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength);
    }

    private int normalizePage(int page) {
        return Math.max(0, page);
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }

    private <T> T first(List<T> values) {
        return values == null || values.isEmpty() ? null : values.get(0);
    }

    private <T> List<T> items(PaginationResponseDto<T> response) {
        return response == null || response.getItems() == null ? List.of() : response.getItems().stream().limit(contextLimit()).toList();
    }

    private PagingMeta meta(Page<?> page, int requestedPage, int size, String sortBy, String direction) {
        return new PagingMeta(page.getTotalElements(), page.getTotalPages(), normalizePage(requestedPage) + 1, size, sortBy, direction);
    }

    private String message(String key, Object... params) {
        return messageSource.getMessage(key, params, LocaleContextHolder.getLocale());
    }

    private int contextLimit() {
        return clamp(maxContextItems, 1, 5);
    }

    private int historyLimit() {
        return clamp(maxHistoryMessages, 1, 10);
    }

    private int messageLimit() {
        return clamp(maxMessageLength, 1, 1000);
    }

    private Map<String, Object> mapOf(Object... pairs) {
        Map<String, Object> value = new LinkedHashMap<>();
        for (int index = 0; index < pairs.length; index += 2) {
            Object item = pairs[index + 1];
            if (item != null) value.put(String.valueOf(pairs[index]), item);
        }
        return value;
    }

    private record ContextResult(List<Map<String, Object>> contextItems,
                                 List<ChatSourceResponse> sources,
                                 List<ChatActionResponse> actions) {
        static ContextResult empty() {
            return new ContextResult(List.of(), List.of(), List.of());
        }
    }

    private record GeneratedAnswer(String answer, ChatResponseMode mode) {
    }
}
