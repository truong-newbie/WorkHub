package org.example.workhub.service;

import org.example.workhub.domain.dto.pagination.PaginationResponseDto;
import org.example.workhub.domain.dto.response.RecommendedJobResponse;
import org.springframework.data.domain.Pageable;

public interface JobRecommendationService {

    PaginationResponseDto<RecommendedJobResponse> getLatestJobs(Pageable pageable);

    PaginationResponseDto<RecommendedJobResponse> getRecommendedJobs(Pageable pageable);
}
