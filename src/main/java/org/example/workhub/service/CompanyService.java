package org.example.workhub.service;

import org.example.workhub.domain.dto.pagination.PaginationFullRequestDto;
import org.example.workhub.domain.dto.pagination.PaginationResponseDto;
import org.example.workhub.domain.dto.request.CompanyRequestDto;
import org.example.workhub.domain.dto.response.CompanyResponseDto;

public interface CompanyService {
    PaginationResponseDto<CompanyResponseDto> getAll(PaginationFullRequestDto request);

    CompanyResponseDto getById(Long id);

    CompanyResponseDto create(CompanyRequestDto request);

    CompanyResponseDto update(Long id, CompanyRequestDto request);

    void delete(Long id);
}
