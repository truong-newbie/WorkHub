package org.example.workhub.service;

import org.example.workhub.domain.dto.pagination.PaginationResponseDto;
import org.example.workhub.domain.dto.request.CompanyJoinRequestCreateRequest;
import org.example.workhub.domain.dto.request.CompanyJoinRequestReviewRequest;
import org.example.workhub.domain.dto.response.CompanyJoinRequestResponse;

public interface CompanyJoinRequestService {

    CompanyJoinRequestResponse requestJoinCompany(Long companyId, CompanyJoinRequestCreateRequest request);

    PaginationResponseDto<CompanyJoinRequestResponse> getMyRequests(int page, int size);

    PaginationResponseDto<CompanyJoinRequestResponse> getCompanyRequests(Long companyId, int page, int size);

    CompanyJoinRequestResponse approve(Long requestId, CompanyJoinRequestReviewRequest request);

    CompanyJoinRequestResponse reject(Long requestId, CompanyJoinRequestReviewRequest request);
}
