package org.example.workhub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.workhub.base.RestApiV1;
import org.example.workhub.base.VsResponseUtil;
import org.example.workhub.constant.UrlConstant;
import org.example.workhub.service.JobRecommendationService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestApiV1
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
public class JobRecommendationController {

    JobRecommendationService jobRecommendationService;

    @Operation(summary = "Get latest published jobs")
    @GetMapping(UrlConstant.JobRecommendation.LATEST_JOBS)
    public ResponseEntity<?> getLatestJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        return VsResponseUtil.success(jobRecommendationService.getLatestJobs(buildPageable(page, size, sortBy, sortDir)));
    }

    @Operation(summary = "Get recommended jobs for current candidate", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(UrlConstant.JobRecommendation.RECOMMENDED_JOBS)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getRecommendedJobs(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "false") boolean refresh,
            @RequestParam(defaultValue = "true") boolean explain) {
        int resolvedPageSize = pageSize == null ? (size == null ? 10 : size) : pageSize;
        int resolvedPage = page == null ? pageNum - 1 : page;
        return VsResponseUtil.success(jobRecommendationService.getRecommendedJobs(
                PageRequest.of(normalizePage(resolvedPage), normalizeSize(resolvedPageSize)),
                location,
                refresh,
                explain
        ));
    }

    private Pageable buildPageable(int page, int size, String sortBy, String sortDir) {
        Sort.Direction direction = "ASC".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(normalizePage(page), normalizeSize(size), direction, normalizeJobSortBy(sortBy));
    }

    private int normalizePage(int page) {
        return Math.max(page, 0);
    }

    private int normalizeSize(int size) {
        if (size < 1) {
            return 10;
        }
        return Math.min(size, 100);
    }

    private String normalizeJobSortBy(String sortBy) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return "createdDate";
        }
        return switch (sortBy.trim()) {
            case "id", "title", "location", "salaryMin", "salaryMax", "experienceYears", "createdDate", "lastModifiedDate" -> sortBy.trim();
            default -> "createdDate";
        };
    }
}
