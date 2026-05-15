package org.example.workhub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.example.workhub.base.RestApiV1;
import org.example.workhub.base.VsResponseUtil;
import org.example.workhub.constant.UrlConstant;
import org.example.workhub.domain.dto.pagination.PaginationResponseDto;
import org.example.workhub.domain.dto.response.FavoriteJobResponse;
import org.example.workhub.service.FavoriteJobService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Log4j2
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
@RestApiV1
@Tag(name = "Favorite Job Controller", description = "APIs for favorite job management")
public class FavoriteJobController {

    FavoriteJobService favoriteJobService;

    @Operation(summary = "Save a job to favorites", description = "Save a job to favorites (Candidate)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Job saved to favorites"),
            @ApiResponse(responseCode = "400", description = "Already in favorites"),
            @ApiResponse(responseCode = "404", description = "Job not found")
    })
    @PostMapping(UrlConstant.FavoriteJob.SAVE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> saveFavoriteJob(
            @PathVariable @Parameter(description = "Job ID") Long jobId) {
        return VsResponseUtil.success(HttpStatus.CREATED, favoriteJobService.saveFavoriteJob(jobId));
    }

    @Operation(summary = "Remove job from favorites", description = "Remove a job from favorites")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Job removed from favorites"),
            @ApiResponse(responseCode = "404", description = "Job not in favorites")
    })
    @DeleteMapping(UrlConstant.FavoriteJob.REMOVE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> removeFavoriteJob(
            @PathVariable @Parameter(description = "Job ID") Long jobId) {
        favoriteJobService.removeFavoriteJob(jobId);
        return VsResponseUtil.success("Job removed from favorites");
    }

    @Operation(summary = "Get my favorite jobs", description = "Get current user's favorite jobs")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Favorites retrieved successfully")
    })
    @GetMapping(UrlConstant.FavoriteJob.MY_FAVORITES)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyFavorites(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return VsResponseUtil.success(favoriteJobService.getMyFavorites(page, size));
    }
}