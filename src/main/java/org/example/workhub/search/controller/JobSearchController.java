package org.example.workhub.search.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.workhub.base.RestApiV1;
import org.example.workhub.base.VsResponseUtil;
import org.example.workhub.search.dto.request.JobSearchRequest;
import org.example.workhub.search.service.JobSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
@RestApiV1
public class JobSearchController {

    JobSearchService jobSearchService;

    @GetMapping("/jobs/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> search(@ModelAttribute JobSearchRequest request) {
        return VsResponseUtil.success(jobSearchService.search(request));
    }

    @GetMapping("/jobs/search/autocomplete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> autocomplete(@RequestParam String keyword,
                                          @RequestParam(defaultValue = "10") int limit) {
        return VsResponseUtil.success(jobSearchService.autocomplete(keyword, limit));
    }

    @PostMapping("/jobs/search/reindex")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> reindex() {
        return VsResponseUtil.success(jobSearchService.reindexAll());
    }
}
