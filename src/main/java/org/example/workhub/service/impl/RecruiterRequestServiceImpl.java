package org.example.workhub.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.workhub.constant.ErrorMessage;
import org.example.workhub.constant.RecruiterRequestStatus;
import org.example.workhub.constant.RoleConstant;
import org.example.workhub.domain.dto.pagination.PaginationResponseDto;
import org.example.workhub.domain.dto.pagination.PagingMeta;
import org.example.workhub.domain.dto.request.RecruiterRequestCreateRequest;
import org.example.workhub.domain.dto.request.RecruiterRequestReviewRequest;
import org.example.workhub.domain.dto.response.RecruiterRequestResponse;
import org.example.workhub.domain.entity.RecruiterRequest;
import org.example.workhub.domain.entity.Role;
import org.example.workhub.domain.entity.User;
import org.example.workhub.exception.BadRequestException;
import org.example.workhub.exception.ConflictException;
import org.example.workhub.exception.ForbiddenException;
import org.example.workhub.exception.NotFoundException;
import org.example.workhub.repository.RecruiterRequestRepository;
import org.example.workhub.repository.RoleRepository;
import org.example.workhub.repository.UserRepository;
import org.example.workhub.security.UserPrincipal;
import org.example.workhub.service.RecruiterRequestService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RecruiterRequestServiceImpl implements RecruiterRequestService {

    private final RecruiterRequestRepository recruiterRequestRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public RecruiterRequestResponse create(RecruiterRequestCreateRequest request) {
        User user = getCurrentUser();
        validateCandidate(user);

        if (recruiterRequestRepository.existsByUserIdAndStatus(user.getId(), RecruiterRequestStatus.PENDING)) {
            throw new ConflictException(ErrorMessage.RecruiterRequest.ERR_PENDING_EXISTS);
        }

        RecruiterRequest recruiterRequest = new RecruiterRequest();
        recruiterRequest.setUser(user);
        recruiterRequest.setStatus(RecruiterRequestStatus.PENDING);
        recruiterRequest.setMessage(trimToNull(request == null ? null : request.getMessage()));
        return toResponse(recruiterRequestRepository.save(recruiterRequest));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponseDto<RecruiterRequestResponse> getMyRequests(int page, int size) {
        UserPrincipal principal = getCurrentUserPrincipal();
        Page<RecruiterRequest> requests = recruiterRequestRepository.findByUserIdOrderByCreatedDateDesc(
                principal.getId(),
                buildPageable(page, size));
        return toPageResponse(requests);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponseDto<RecruiterRequestResponse> getRequests(
            RecruiterRequestStatus status,
            int page,
            int size) {
        validateAdmin(getCurrentUserPrincipal());
        Pageable pageable = buildPageable(page, size);
        Page<RecruiterRequest> requests = status == null
                ? recruiterRequestRepository.findAllByOrderByCreatedDateDesc(pageable)
                : recruiterRequestRepository.findByStatusOrderByCreatedDateDesc(status, pageable);
        return toPageResponse(requests);
    }

    @Override
    public RecruiterRequestResponse approve(Long requestId, RecruiterRequestReviewRequest request) {
        User reviewer = getCurrentAdmin();
        RecruiterRequest recruiterRequest = getRequestOrThrow(requestId);
        validatePending(recruiterRequest);

        User user = recruiterRequest.getUser();
        validateEligibleForApproval(user);
        Role recruiterRole = roleRepository.findByName(RoleConstant.RECRUITER)
                .orElseThrow(() -> new NotFoundException(
                        ErrorMessage.Role.ERR_NOT_FOUND,
                        new String[]{RoleConstant.RECRUITER}));
        user.setRole(recruiterRole);
        userRepository.save(user);

        recruiterRequest.setStatus(RecruiterRequestStatus.APPROVED);
        applyReview(recruiterRequest, reviewer, request);
        rejectOtherPendingRequests(user.getId(), recruiterRequest.getId(), reviewer);
        return toResponse(recruiterRequestRepository.save(recruiterRequest));
    }

    @Override
    public RecruiterRequestResponse reject(Long requestId, RecruiterRequestReviewRequest request) {
        User reviewer = getCurrentAdmin();
        RecruiterRequest recruiterRequest = getRequestOrThrow(requestId);
        validatePending(recruiterRequest);

        recruiterRequest.setStatus(RecruiterRequestStatus.REJECTED);
        applyReview(recruiterRequest, reviewer, request);
        return toResponse(recruiterRequestRepository.save(recruiterRequest));
    }

    private void applyReview(
            RecruiterRequest recruiterRequest,
            User reviewer,
            RecruiterRequestReviewRequest request) {
        recruiterRequest.setReviewNote(trimToNull(request == null ? null : request.getReviewNote()));
        recruiterRequest.setReviewedBy(reviewer);
        recruiterRequest.setReviewedAt(LocalDateTime.now());
    }

    private void rejectOtherPendingRequests(String userId, Long approvedRequestId, User reviewer) {
        List<RecruiterRequest> pendingRequests = recruiterRequestRepository
                .findByUserIdAndStatusAndIdNot(userId, RecruiterRequestStatus.PENDING, approvedRequestId);
        for (RecruiterRequest pendingRequest : pendingRequests) {
            pendingRequest.setStatus(RecruiterRequestStatus.REJECTED);
            pendingRequest.setReviewNote("Auto rejected because another recruiter request was approved");
            pendingRequest.setReviewedBy(reviewer);
            pendingRequest.setReviewedAt(LocalDateTime.now());
        }
        recruiterRequestRepository.saveAll(pendingRequests);
    }

    private RecruiterRequest getRequestOrThrow(Long requestId) {
        return recruiterRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.RecruiterRequest.ERR_NOT_FOUND));
    }

    private User getCurrentAdmin() {
        UserPrincipal principal = getCurrentUserPrincipal();
        validateAdmin(principal);
        return getUser(principal.getId());
    }

    private User getCurrentUser() {
        return getUser(getCurrentUserPrincipal().getId());
    }

    private User getUser(String userId) {
        return userRepository.findById(userId)
                .filter(user -> !Boolean.TRUE.equals(user.getDeleted()))
                .orElseThrow(() -> new NotFoundException(
                        ErrorMessage.User.ERR_NOT_FOUND_ID,
                        new String[]{userId}));
    }

    private void validateCandidate(User user) {
        if (hasRole(user, RoleConstant.RECRUITER)) {
            throw new BadRequestException(ErrorMessage.RecruiterRequest.ERR_ALREADY_RECRUITER);
        }
        if (!hasRole(user, RoleConstant.CANDIDATE)) {
            throw new ForbiddenException(ErrorMessage.RecruiterRequest.ERR_ONLY_CANDIDATE);
        }
    }

    private void validateEligibleForApproval(User user) {
        if (hasRole(user, RoleConstant.RECRUITER)) {
            throw new BadRequestException(ErrorMessage.RecruiterRequest.ERR_ALREADY_RECRUITER);
        }
        if (!hasRole(user, RoleConstant.CANDIDATE)) {
            throw new BadRequestException(ErrorMessage.RecruiterRequest.ERR_CANDIDATE_NOT_ELIGIBLE);
        }
    }

    private void validatePending(RecruiterRequest recruiterRequest) {
        if (recruiterRequest.getStatus() != RecruiterRequestStatus.PENDING) {
            throw new BadRequestException(ErrorMessage.RecruiterRequest.ERR_NOT_PENDING);
        }
    }

    private void validateAdmin(UserPrincipal principal) {
        if (!hasRole(principal, RoleConstant.ADMIN)) {
            throw new ForbiddenException(ErrorMessage.FORBIDDEN);
        }
    }

    private boolean hasRole(User user, String role) {
        return user.getRole() != null && role.equals(user.getRole().getName());
    }

    private boolean hasRole(UserPrincipal principal, String role) {
        return principal.getAuthorities() != null
                && principal.getAuthorities().stream()
                .anyMatch(authority -> role.equals(authority.getAuthority()));
    }

    private UserPrincipal getCurrentUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken
                || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new ForbiddenException(ErrorMessage.UNAUTHORIZED);
        }
        return principal;
    }

    private Pageable buildPageable(int page, int size) {
        int pageNumber = Math.max(page, 0);
        int pageSize = size < 1 ? 10 : Math.min(size, 100);
        return PageRequest.of(pageNumber, pageSize, Sort.by("createdDate").descending());
    }

    private PaginationResponseDto<RecruiterRequestResponse> toPageResponse(Page<RecruiterRequest> page) {
        PagingMeta meta = new PagingMeta(
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber() + 1,
                page.getSize(),
                "createdDate",
                "DESC");
        List<RecruiterRequestResponse> items = page.getContent().stream()
                .map(this::toResponse)
                .toList();
        return new PaginationResponseDto<>(meta, items);
    }

    private RecruiterRequestResponse toResponse(RecruiterRequest recruiterRequest) {
        return RecruiterRequestResponse.builder()
                .id(recruiterRequest.getId())
                .status(recruiterRequest.getStatus())
                .message(recruiterRequest.getMessage())
                .reviewNote(recruiterRequest.getReviewNote())
                .reviewedAt(recruiterRequest.getReviewedAt())
                .createdDate(recruiterRequest.getCreatedDate())
                .user(toUserSummary(recruiterRequest.getUser()))
                .reviewedBy(toReviewerSummary(recruiterRequest.getReviewedBy()))
                .build();
    }

    private RecruiterRequestResponse.UserSummary toUserSummary(User user) {
        if (user == null) {
            return null;
        }
        return RecruiterRequestResponse.UserSummary.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roleName(user.getRole() == null ? null : user.getRole().getName())
                .build();
    }

    private RecruiterRequestResponse.ReviewerSummary toReviewerSummary(User reviewer) {
        if (reviewer == null) {
            return null;
        }
        return RecruiterRequestResponse.ReviewerSummary.builder()
                .id(reviewer.getId())
                .username(reviewer.getUsername())
                .email(reviewer.getEmail())
                .build();
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
