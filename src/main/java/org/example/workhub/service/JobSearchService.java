package org.example.workhub.service;

import org.example.workhub.domain.dto.pagination.PaginationResponseDto;
import org.example.workhub.domain.dto.request.JobSearchRequest;
import org.example.workhub.domain.dto.response.JobSearchResponse;
import org.example.workhub.domain.dto.response.JobSuggestionResponse;
import org.example.workhub.domain.dto.response.SearchReindexResponse;

import java.util.List;

public interface JobSearchService {

    PaginationResponseDto<JobSearchResponse> search(JobSearchRequest request);

    List<JobSuggestionResponse> autocomplete(String keyword, int limit);

    void indexJob(Long jobId);

    void deleteJob(Long jobId);

    SearchReindexResponse reindexAll();
}
