package org.example.workhub.service;

import org.example.workhub.domain.dto.pagination.PaginationResponseDto;
import org.example.workhub.domain.dto.response.FavoriteJobResponse;

public interface FavoriteJobService {

    // ========== Actions ==========
    FavoriteJobResponse saveFavoriteJob(Long jobId);

    void removeFavoriteJob(Long jobId);

    PaginationResponseDto<FavoriteJobResponse> getMyFavorites(int page, int size);

    boolean isFavorite(Long jobId);
}