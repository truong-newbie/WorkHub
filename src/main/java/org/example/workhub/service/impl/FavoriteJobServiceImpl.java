package org.example.workhub.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.workhub.constant.ErrorMessage;
import org.example.workhub.domain.dto.pagination.PagingMeta;
import org.example.workhub.domain.dto.pagination.PaginationResponseDto;
import org.example.workhub.domain.dto.response.FavoriteJobResponse;
import org.example.workhub.domain.entity.FavoriteJob;
import org.example.workhub.domain.entity.Job;
import org.example.workhub.domain.entity.User;
import org.example.workhub.domain.mapper.FavoriteJobMapper;
import org.example.workhub.exception.ConflictException;
import org.example.workhub.exception.ForbiddenException;
import org.example.workhub.exception.NotFoundException;
import org.example.workhub.repository.FavoriteJobRepository;
import org.example.workhub.repository.JobRepository;
import org.example.workhub.repository.UserRepository;
import org.example.workhub.security.UserPrincipal;
import org.example.workhub.service.FavoriteJobService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FavoriteJobServiceImpl implements FavoriteJobService {

    private final FavoriteJobRepository favoriteJobRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final FavoriteJobMapper favoriteJobMapper;

    @Override
    public FavoriteJobResponse saveFavoriteJob(Long jobId) {
        UserPrincipal currentUser = getCurrentUserPrincipal();
        User user = getUserFromPrincipal(currentUser);

        // Find job
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Job.ERR_NOT_FOUND_ID, new String[]{String.valueOf(jobId)}));

        if (Boolean.TRUE.equals(job.getDeleted())) {
            throw new NotFoundException(ErrorMessage.Job.ERR_NOT_FOUND_ID, new String[]{String.valueOf(jobId)});
        }

        // Check if already favorited
        if (favoriteJobRepository.existsByJobIdAndUserIdAndDeletedFalse(jobId, currentUser.getId())) {
            throw new ConflictException(ErrorMessage.Favorite.ERR_ALREADY_FAVORITE);
        }

        // Create favorite
        FavoriteJob favorite = new FavoriteJob();
        favorite.setUser(user);
        favorite.setJob(job);
        favorite.setCreatedAt(Instant.now());

        FavoriteJob saved = favoriteJobRepository.save(favorite);
        return favoriteJobMapper.toResponse(saved);
    }

    @Override
    public void removeFavoriteJob(Long jobId) {
        UserPrincipal currentUser = getCurrentUserPrincipal();

        FavoriteJob favorite = favoriteJobRepository.findByJobIdAndUserId(jobId, currentUser.getId())
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Favorite.ERR_NOT_FAVORITE));

        favorite.setDeleted(true);
        favoriteJobRepository.save(favorite);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponseDto<FavoriteJobResponse> getMyFavorites(int page, int size) {
        UserPrincipal currentUser = getCurrentUserPrincipal();
        Pageable pageable = PageRequest.of(page, size);

        Page<FavoriteJob> favoritePage = favoriteJobRepository.findByUserId(currentUser.getId(), pageable);

        PagingMeta pagingMeta = new PagingMeta(
                favoritePage.getTotalElements(),
                favoritePage.getTotalPages(),
                page + 1,
                size,
                "createdAt",
                "DESC"
        );

        return new PaginationResponseDto<>(pagingMeta, favoriteJobMapper.toResponses(favoritePage.getContent()));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFavorite(Long jobId) {
        UserPrincipal currentUser = getCurrentUserPrincipal();
        return favoriteJobRepository.existsByJobIdAndUserIdAndDeletedFalse(jobId, currentUser.getId());
    }

    // ========== Helper Methods ==========

    private UserPrincipal getCurrentUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ForbiddenException(ErrorMessage.UNAUTHORIZED);
        }
        return (UserPrincipal) authentication.getPrincipal();
    }

    private User getUserFromPrincipal(UserPrincipal principal) {
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{principal.getId()}));
    }
}