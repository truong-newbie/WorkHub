package org.example.workhub.service;

import org.example.workhub.constant.RecruiterRequestStatus;
import org.example.workhub.domain.dto.pagination.PaginationResponseDto;
import org.example.workhub.domain.dto.request.RecruiterRequestCreateRequest;
import org.example.workhub.domain.dto.request.RecruiterRequestReviewRequest;
import org.example.workhub.domain.dto.response.RecruiterRequestResponse;

public interface RecruiterRequestService {

    RecruiterRequestResponse create(RecruiterRequestCreateRequest request);

    PaginationResponseDto<RecruiterRequestResponse> getMyRequests(int page, int size);

    PaginationResponseDto<RecruiterRequestResponse> getRequests(RecruiterRequestStatus status, int page, int size);

    RecruiterRequestResponse approve(Long requestId, RecruiterRequestReviewRequest request);

    RecruiterRequestResponse reject(Long requestId, RecruiterRequestReviewRequest request);
}
