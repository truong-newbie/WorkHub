package org.example.workhub.service.impl;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.json.JsonData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.workhub.constant.ErrorMessage;
import org.example.workhub.domain.document.JobSearchDocument;
import org.example.workhub.domain.dto.pagination.PaginationResponseDto;
import org.example.workhub.domain.dto.pagination.PagingMeta;
import org.example.workhub.domain.dto.request.JobFilterRequest;
import org.example.workhub.domain.dto.request.JobSearchRequest;
import org.example.workhub.domain.dto.response.JobSearchResponse;
import org.example.workhub.domain.dto.response.JobSuggestionResponse;
import org.example.workhub.domain.dto.response.SearchReindexResponse;
import org.example.workhub.domain.entity.Job;
import org.example.workhub.domain.mapper.JobSearchMapper;
import org.example.workhub.domain.specification.JobSpecification;
import org.example.workhub.exception.InternalServerException;
import org.example.workhub.exception.NotFoundException;
import org.example.workhub.repository.JobRepository;
import org.example.workhub.service.JobSearchService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightParameters;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobSearchServiceImpl implements JobSearchService {

    private static final String SORT_SCORE = "_score";

    private final JobRepository jobRepository;
    private final JobSearchMapper jobSearchMapper;
    private final ElasticsearchOperations elasticsearchOperations;

    @Value("${search.job.fallback-to-jpa:true}")
    private boolean fallbackToJpa;

    @Override
    @Transactional(readOnly = true)
    public PaginationResponseDto<JobSearchResponse> search(JobSearchRequest request) {
        try {
            Pageable pageable = buildPageable(request);
            NativeQuery query = NativeQuery.builder()
                    .withQuery(buildSearchQuery(request))
                    .withPageable(pageable)
                    .withHighlightQuery(new HighlightQuery(buildHighlight(), JobSearchDocument.class))
                    .build();
            SearchHits<JobSearchDocument> hits = elasticsearchOperations.search(query, JobSearchDocument.class);
            List<JobSearchResponse> items = hits.getSearchHits().stream()
                    .map(jobSearchMapper::toResponse)
                    .toList();
            return new PaginationResponseDto<>(buildMeta(hits.getTotalHits(), request), items);
        } catch (Exception ex) {
            log.error("Job search engine unavailable, fallbackToJpa={}", fallbackToJpa, ex);
            if (fallbackToJpa) {
                return fallbackSearch(request);
            }
            throw new InternalServerException(ErrorMessage.Search.ERR_ENGINE_UNAVAILABLE);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobSuggestionResponse> autocomplete(String keyword, int limit) {
        if (!StringUtils.hasText(keyword)) {
            return List.of();
        }
        int safeLimit = Math.max(1, Math.min(limit, 50));
        try {
            NativeQuery query = NativeQuery.builder()
                    .withQuery(buildAutocompleteQuery(keyword))
                    .withPageable(PageRequest.of(0, safeLimit))
                    .build();
            SearchHits<JobSearchDocument> hits = elasticsearchOperations.search(query, JobSearchDocument.class);
            return toSuggestions(hits.getSearchHits(), safeLimit);
        } catch (Exception ex) {
            log.error("Job autocomplete engine unavailable", ex);
            if (fallbackToJpa) {
                return fallbackAutocomplete(keyword, safeLimit);
            }
            throw new InternalServerException(ErrorMessage.Search.ERR_ENGINE_UNAVAILABLE);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void indexJob(Long jobId) {
        try {
            Job job = jobRepository.findByIdForSearch(jobId)
                    .orElseThrow(() -> new NotFoundException(ErrorMessage.Job.ERR_NOT_FOUND_ID, new String[]{String.valueOf(jobId)}));
            ensureIndexExists();
            elasticsearchOperations.save(jobSearchMapper.toDocument(job));
        } catch (Exception ex) {
            log.error("Failed to sync job {} to search index", jobId, ex);
        }
    }

    @Override
    public void deleteJob(Long jobId) {
        try {
            elasticsearchOperations.delete(String.valueOf(jobId), JobSearchDocument.class);
        } catch (Exception ex) {
            log.error("Failed to delete job {} from search index", jobId, ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public SearchReindexResponse reindexAll() {
        LocalDateTime startedAt = LocalDateTime.now();
        long startedNanos = System.nanoTime();
        try {
            IndexOperations indexOperations = elasticsearchOperations.indexOps(JobSearchDocument.class);
            if (indexOperations.exists()) {
                indexOperations.delete();
            }
            indexOperations.createWithMapping();
            List<JobSearchDocument> documents = jobRepository.findAllForSearch().stream()
                    .map(jobSearchMapper::toDocument)
                    .toList();
            elasticsearchOperations.save(documents);
            LocalDateTime finishedAt = LocalDateTime.now();
            return SearchReindexResponse.builder()
                    .indexedCount(documents.size())
                    .startedAt(startedAt)
                    .finishedAt(finishedAt)
                    .durationMs(Duration.ofNanos(System.nanoTime() - startedNanos).toMillis())
                    .build();
        } catch (Exception ex) {
            log.error("Failed to rebuild job search index", ex);
            throw new InternalServerException(ErrorMessage.Search.ERR_REINDEX_FAILED);
        }
    }

    private Query buildSearchQuery(JobSearchRequest request) {
        List<Query> filters = basePublicFilters();
        if (request.getCompanyId() != null) {
            filters.add(termQuery("companyId", request.getCompanyId()));
        }
        if (StringUtils.hasText(request.getLevel())) {
            filters.add(termQuery("level", request.getLevel().trim()));
        }
        if (StringUtils.hasText(request.getEmploymentType())) {
            filters.add(termQuery("employmentType", request.getEmploymentType().trim()));
        }
        if (request.getSkillIds() != null && !request.getSkillIds().isEmpty()) {
            filters.add(Query.of(q -> q.terms(t -> t.field("skillIds").terms(v -> v.value(
                    request.getSkillIds().stream().map(FieldValue::of).toList()
            )))));
        }
        if (request.getSalaryMin() != null) {
            filters.add(Query.of(q -> q.bool(b -> b
                    .should(s -> s.term(t -> t.field("negotiableSalary").value(true)))
                    .should(s -> s.range(r -> r.field("salaryMax").gte(JsonData.of(request.getSalaryMin()))))
                    .minimumShouldMatch("1")
            )));
        }
        if (request.getSalaryMax() != null) {
            filters.add(Query.of(q -> q.bool(b -> b
                    .should(s -> s.term(t -> t.field("negotiableSalary").value(true)))
                    .should(s -> s.range(r -> r.field("salaryMin").lte(JsonData.of(request.getSalaryMax()))))
                    .minimumShouldMatch("1")
            )));
        }

        List<Query> must = new ArrayList<>();
        if (StringUtils.hasText(request.getKeyword())) {
            must.add(keywordQuery(request.getKeyword().trim()));
        }
        if (StringUtils.hasText(request.getLocation())) {
            must.add(Query.of(q -> q.match(m -> m.field("location").query(request.getLocation().trim()).fuzziness("AUTO"))));
        }
        if (request.getSkillNames() != null && !request.getSkillNames().isEmpty()) {
            must.add(Query.of(q -> q.bool(b -> {
                request.getSkillNames().stream()
                        .filter(StringUtils::hasText)
                        .forEach(skill -> b.should(s -> s.match(m -> m.field("skillNames").query(skill.trim()).fuzziness("AUTO"))));
                return b.minimumShouldMatch("1");
            })));
        }

        return Query.of(q -> q.bool(b -> b.must(must).filter(filters)));
    }

    private void ensureIndexExists() {
        IndexOperations indexOperations = elasticsearchOperations.indexOps(JobSearchDocument.class);
        if (!indexOperations.exists()) {
            indexOperations.createWithMapping();
        }
    }

    private Query buildAutocompleteQuery(String keyword) {
        return Query.of(q -> q.bool(b -> b
                .must(basePublicFilters())
                .should(s -> s.matchPhrasePrefix(m -> m.field("title").query(keyword).boost(5.0f)))
                .should(s -> s.matchPhrasePrefix(m -> m.field("skillNames").query(keyword).boost(4.0f)))
                .should(s -> s.matchPhrasePrefix(m -> m.field("companyName").query(keyword).boost(2.0f)))
                .minimumShouldMatch("1")
        ));
    }

    private Query keywordQuery(String keyword) {
        return Query.of(q -> q.bool(b -> b
                .should(s -> s.multiMatch(m -> m
                        .query(keyword)
                        .fields("title^5", "skillNames^4", "requirement^2", "companyName^1.5", "description^1", "benefit^1")
                        .fuzziness("AUTO")
                        .minimumShouldMatch("70%")
                ))
                .should(s -> s.matchPhrasePrefix(m -> m.field("title").query(keyword).boost(4.0f)))
                .should(s -> s.matchPhrasePrefix(m -> m.field("skillNames").query(keyword).boost(3.0f)))
                .minimumShouldMatch("1")
        ));
    }

    private List<Query> basePublicFilters() {
        return List.of(
                termQuery("published", true),
                termQuery("deleted", false),
                termQuery("companyActive", true),
                termQuery("companyVerified", true),
                Query.of(q -> q.bool(b -> b
                        .should(s -> s.range(r -> r.field("expiredAt").gt(JsonData.of(Instant.now().toString()))))
                        .should(s -> s.bool(bb -> bb.mustNot(mn -> mn.exists(e -> e.field("expiredAt")))))
                        .minimumShouldMatch("1")
                ))
        );
    }

    private Query termQuery(String field, Object value) {
        return Query.of(q -> q.term(t -> t.field(field).value(toFieldValue(value))));
    }

    private FieldValue toFieldValue(Object value) {
        if (value instanceof Boolean booleanValue) {
            return FieldValue.of(booleanValue);
        }
        if (value instanceof Integer integerValue) {
            return FieldValue.of(integerValue.longValue());
        }
        if (value instanceof Long longValue) {
            return FieldValue.of(longValue);
        }
        if (value instanceof Float floatValue) {
            return FieldValue.of(floatValue.doubleValue());
        }
        if (value instanceof Double doubleValue) {
            return FieldValue.of(doubleValue);
        }
        return FieldValue.of(String.valueOf(value));
    }

    private Highlight buildHighlight() {
        HighlightParameters parameters = HighlightParameters.builder()
                .withPreTags("<em>")
                .withPostTags("</em>")
                .build();
        return new Highlight(parameters, List.of(
                new HighlightField("title"),
                new HighlightField("description"),
                new HighlightField("requirement"),
                new HighlightField("skillNames")
        ));
    }

    private Pageable buildPageable(JobSearchRequest request) {
        int pageNum = request.getPageNum();
        int pageSize = request.getPageSize();
        String sortBy = normalizeSortBy(request.getSortBy());
        if (SORT_SCORE.equals(sortBy)) {
            return PageRequest.of(pageNum, pageSize);
        }
        Sort.Direction direction = Boolean.TRUE.equals(request.getIsAscending()) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(pageNum, pageSize, direction, sortBy);
    }

    private String normalizeSortBy(String sortBy) {
        if (!StringUtils.hasText(sortBy)) {
            return SORT_SCORE;
        }
        return switch (sortBy.trim()) {
            case "createdDate", "expiredAt", "salaryMin", "salaryMax", "experienceYears", "title" -> sortBy.trim();
            default -> SORT_SCORE;
        };
    }

    private PagingMeta buildMeta(long totalHits, JobSearchRequest request) {
        int pageSize = request.getPageSize();
        int totalPages = pageSize == 0 ? 0 : (int) Math.ceil((double) totalHits / pageSize);
        String sortBy = normalizeSortBy(request.getSortBy());
        return new PagingMeta(
                totalHits,
                totalPages,
                request.getPageNum() + 1,
                pageSize,
                sortBy,
                SORT_SCORE.equals(sortBy) ? "DESC" : Boolean.TRUE.equals(request.getIsAscending()) ? "ASC" : "DESC"
        );
    }

    private PaginationResponseDto<JobSearchResponse> fallbackSearch(JobSearchRequest request) {
        JobFilterRequest filter = new JobFilterRequest();
        filter.setKeyword(request.getKeyword());
        filter.setLocation(request.getLocation());
        filter.setLevel(request.getLevel());
        filter.setCompanyId(request.getCompanyId());
        filter.setSalaryMin(request.getSalaryMin() != null ? request.getSalaryMin().toPlainString() : null);
        filter.setSalaryMax(request.getSalaryMax() != null ? request.getSalaryMax().toPlainString() : null);
        filter.setEmploymentType(request.getEmploymentType());
        filter.setPublished(true);
        filter.setIncludeExpired(false);
        filter.setPage(Math.max(0, request.getPageNum()));
        filter.setSize(request.getPageSize());
        filter.setSortBy("createdDate");
        filter.setSortDir("DESC");

        Specification<Job> spec = JobSpecification.search(filter.getKeyword())
                .and(JobSpecification.withFilters(filter))
                .and((root, query, cb) -> cb.and(
                        cb.isTrue(root.get("company").get("active")),
                        cb.isTrue(root.get("company").get("verified"))
                ));
        if (request.getSkillIds() != null && !request.getSkillIds().isEmpty()) {
            spec = spec.and(JobSpecification.hasSkill(request.getSkillIds()));
        }

        Page<Job> page = jobRepository.findAll(spec, PageRequest.of(filter.getPage(), filter.getSize(), Sort.by("createdDate").descending()));
        List<JobSearchResponse> items = page.getContent().stream()
                .map(jobSearchMapper::toDocument)
                .map(jobSearchMapper::toResponse)
                .toList();
        return new PaginationResponseDto<>(
                new PagingMeta(page.getTotalElements(), page.getTotalPages(), filter.getPage() + 1, filter.getSize(), "createdDate", "DESC"),
                items
        );
    }

    private List<JobSuggestionResponse> fallbackAutocomplete(String keyword, int limit) {
        JobFilterRequest request = new JobFilterRequest();
        request.setKeyword(keyword);
        request.setPage(0);
        request.setSize(limit);
        request.setPublished(true);
        request.setIncludeExpired(false);
        return fallbackSearch(toSearchRequest(request)).getItems().stream()
                .map(job -> new JobSuggestionResponse(job.getTitle(), "JOB_TITLE", job.getId(), null, job.getCompanyId()))
                .toList();
    }

    private JobSearchRequest toSearchRequest(JobFilterRequest filter) {
        JobSearchRequest request = new JobSearchRequest();
        request.setKeyword(filter.getKeyword());
        request.setPageNum(filter.getPage() + 1);
        request.setPageSize(filter.getSize());
        return request;
    }

    private List<JobSuggestionResponse> toSuggestions(List<SearchHit<JobSearchDocument>> hits, int limit) {
        Map<String, JobSuggestionResponse> suggestions = new LinkedHashMap<>();
        for (SearchHit<JobSearchDocument> hit : hits) {
            JobSearchDocument document = hit.getContent();
            putSuggestion(suggestions, document.getTitle(), "JOB_TITLE", document.getId(), null, document.getCompanyId(), limit);
            putSuggestion(suggestions, document.getCompanyName(), "COMPANY", null, null, document.getCompanyId(), limit);
            if (document.getSkillNames() != null) {
                for (int i = 0; i < document.getSkillNames().size(); i++) {
                    Long skillId = document.getSkillIds() != null && document.getSkillIds().size() > i ? document.getSkillIds().get(i) : null;
                    putSuggestion(suggestions, document.getSkillNames().get(i), "SKILL", null, skillId, null, limit);
                }
            }
            if (suggestions.size() >= limit) {
                break;
            }
        }
        return suggestions.values().stream().limit(limit).toList();
    }

    private void putSuggestion(Map<String, JobSuggestionResponse> suggestions,
                               String text,
                               String type,
                               Long jobId,
                               Long skillId,
                               Long companyId,
                               int limit) {
        if (!StringUtils.hasText(text) || suggestions.size() >= limit) {
            return;
        }
        String key = type + ":" + text.toLowerCase();
        suggestions.putIfAbsent(key, new JobSuggestionResponse(text, type, jobId, skillId, companyId));
    }
}
