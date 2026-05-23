package org.example.workhub.service;

import org.example.workhub.domain.dto.pagination.PaginationResponseDto;
import org.example.workhub.domain.dto.request.CompanySearchRequest;
import org.example.workhub.domain.dto.request.CompanyRequestDto;
import org.example.workhub.domain.dto.response.CompanyResponseDto;
import org.example.workhub.domain.dto.response.CompanyStatisticsResponse;
import org.example.workhub.domain.dto.response.JobResponse;
import org.springframework.web.multipart.MultipartFile;

public interface CompanyService {
    PaginationResponseDto<CompanyResponseDto> getAll(CompanySearchRequest request);

    CompanyResponseDto getById(Long id);

    CompanyResponseDto getCurrentRecruiterCompany();

    CompanyResponseDto create(CompanyRequestDto request);

    CompanyResponseDto update(Long id, CompanyRequestDto request);

    CompanyResponseDto updateCurrentRecruiterCompany(CompanyRequestDto request);

    CompanyResponseDto uploadLogo(Long id, MultipartFile file);

    CompanyResponseDto uploadCover(Long id, MultipartFile file);

    CompanyResponseDto enable(Long id);

    CompanyResponseDto disable(Long id);

    CompanyResponseDto approve(Long id);

    CompanyResponseDto reject(Long id);

    PaginationResponseDto<JobResponse> getCompanyJobs(Long id, CompanySearchRequest request);

    CompanyStatisticsResponse getCompanyStatistics(Long id);

    void delete(Long id);
}
