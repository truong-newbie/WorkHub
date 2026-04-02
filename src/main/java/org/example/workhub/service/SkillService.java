package org.example.workhub.service;

import org.example.workhub.domain.dto.pagination.PaginationFullRequestDto;
import org.example.workhub.domain.dto.pagination.PaginationResponseDto;
import org.example.workhub.domain.dto.request.SkillRequestDto;
import org.example.workhub.domain.dto.response.SkillResponseDto;

public interface SkillService {

    SkillResponseDto create(SkillRequestDto request);

    SkillResponseDto update(Long id, SkillRequestDto request);

    SkillResponseDto getById(Long id);

    PaginationResponseDto<SkillResponseDto> getAll(PaginationFullRequestDto request);

    void delete(Long id);

}
