package org.example.workhub.service;

import org.example.workhub.domain.dto.pagination.PaginationResponseDto;
import org.example.workhub.domain.dto.request.SkillSearchRequest;
import org.example.workhub.domain.dto.request.SkillRequestDto;
import org.example.workhub.domain.dto.response.PopularSkillResponse;
import org.example.workhub.domain.dto.response.SkillResponseDto;
import org.example.workhub.domain.dto.response.SkillSuggestionResponse;

import java.util.List;

public interface SkillService {

    SkillResponseDto create(SkillRequestDto request);

    SkillResponseDto update(Long id, SkillRequestDto request);

    SkillResponseDto getById(Long id);

    PaginationResponseDto<SkillResponseDto> getAll(SkillSearchRequest request);

    SkillResponseDto enable(Long id);

    SkillResponseDto disable(Long id);

    List<SkillSuggestionResponse> getSuggestions(String keyword, int limit);

    List<PopularSkillResponse> getPopularSkills(int limit);

    void delete(Long id);

}
