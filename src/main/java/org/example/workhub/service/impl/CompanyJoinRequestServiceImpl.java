package org.example.workhub.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.workhub.constant.ErrorMessage;
import org.example.workhub.constant.RoleConstant;
import org.example.workhub.constant.StatusEnum;
import org.example.workhub.domain.dto.pagination.PaginationResponseDto;
import org.example.workhub.domain.dto.pagination.PagingMeta;
import org.example.workhub.domain.dto.request.CompanyJoinRequestCreateRequest;
import org.example.workhub.domain.dto.request.CompanyJoinRequestReviewRequest;
import org.example.workhub.domain.dto.response.CompanyJoinRequestResponse;
import org.example.workhub.domain.entity.Company;
import org.example.workhub.domain.entity.CompanyJoinRequest;
import org.example.workhub.domain.entity.User;
import org.example.workhub.exception.BadRequestException;
import org.example.workhub.exception.ConflictException;
import org.example.workhub.exception.ForbiddenException;
import org.example.workhub.exception.NotFoundException;
import org.example.workhub.repository.CompanyJoinRequestRepository;
import org.example.workhub.repository.CompanyRepository;
import org.example.workhub.repository.UserRepository;
import org.example.workhub.security.UserPrincipal;
import org.example.workhub.service.CompanyJoinRequestService;
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
public class CompanyJoinRequestServiceImpl implements CompanyJoinRequestService {

    private final CompanyJoinRequestRepository companyJoinRequestRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    @Override
    public CompanyJoinRequestResponse requestJoinCompany(Long companyId, CompanyJoinRequestCreateRequest request) {
        UserPrincipal principal = getCurrentUserPrincipal();
        validateRecruiter(principal);

        User recruiter = getCurrentUser(principal);
        if (recruiter.getCompany() != null) {
            throw new BadRequestException("Recruiter already belongs to a company");
        }

        Company company = getCompanyOrThrow(companyId);
        validateCompanyCanReceiveJoinRequest(company);

        if (companyJoinRequestRepository.existsByRecruiterIdAndCompanyIdAndStatus(
                recruiter.getId(), company.getId(), StatusEnum.PENDING)) {
            throw new ConflictException("Pending join request already exists for this company");
        }

        CompanyJoinRequest joinRequest = new CompanyJoinRequest();
        joinRequest.setRecruiter(recruiter);
        joinRequest.setCompany(company);
        joinRequest.setStatus(StatusEnum.PENDING);
        joinRequest.setMessage(trimToNull(request == null ? null : request.getMessage()));

        return toResponse(companyJoinRequestRepository.save(joinRequest));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponseDto<CompanyJoinRequestResponse> getMyRequests(int page, int size) {
        UserPrincipal principal = getCurrentUserPrincipal();
        validateRecruiter(principal);

        Pageable pageable = buildPageable(page, size);
        Page<CompanyJoinRequest> requestPage =
                companyJoinRequestRepository.findByRecruiterIdOrderByCreatedDateDesc(principal.getId(), pageable);
        return toPageResponse(requestPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponseDto<CompanyJoinRequestResponse> getCompanyRequests(Long companyId, int page, int size) {
        UserPrincipal principal = getCurrentUserPrincipal();
        Company company = getCompanyOrThrow(companyId);
        validateCanReviewCompanyRequests(company, principal);

        Pageable pageable = buildPageable(page, size);
        Page<CompanyJoinRequest> requestPage =
                companyJoinRequestRepository.findByCompanyIdOrderByCreatedDateDesc(companyId, pageable);
        return toPageResponse(requestPage);
    }

    @Override
    public CompanyJoinRequestResponse approve(Long requestId, CompanyJoinRequestReviewRequest request) {
        UserPrincipal principal = getCurrentUserPrincipal();
        User reviewer = getCurrentUser(principal);
        CompanyJoinRequest joinRequest = getJoinRequestOrThrow(requestId);
        validatePending(joinRequest);
        validateCanReviewCompanyRequests(joinRequest.getCompany(), principal);

        User recruiter = joinRequest.getRecruiter();
        if (recruiter.getCompany() != null) {
            throw new BadRequestException("Recruiter already belongs to a company");
        }

        recruiter.setCompany(joinRequest.getCompany());
        userRepository.save(recruiter);

        joinRequest.setStatus(StatusEnum.APPROVED);
        joinRequest.setReviewNote(trimToNull(request == null ? null : request.getReviewNote()));
        joinRequest.setReviewedBy(reviewer);
        joinRequest.setReviewedAt(LocalDateTime.now());

        rejectOtherPendingRequests(recruiter.getId(), joinRequest.getId(), reviewer);

        return toResponse(companyJoinRequestRepository.save(joinRequest));
    }

    @Override
    public CompanyJoinRequestResponse reject(Long requestId, CompanyJoinRequestReviewRequest request) {
        UserPrincipal principal = getCurrentUserPrincipal();
        User reviewer = getCurrentUser(principal);
        CompanyJoinRequest joinRequest = getJoinRequestOrThrow(requestId);
        validatePending(joinRequest);
        validateCanReviewCompanyRequests(joinRequest.getCompany(), principal);

        joinRequest.setStatus(StatusEnum.REJECTED);
        joinRequest.setReviewNote(trimToNull(request == null ? null : request.getReviewNote()));
        joinRequest.setReviewedBy(reviewer);
        joinRequest.setReviewedAt(LocalDateTime.now());

        return toResponse(companyJoinRequestRepository.save(joinRequest));
    }

    private void rejectOtherPendingRequests(String recruiterId, Long approvedRequestId, User reviewer) {
        List<CompanyJoinRequest> pendingRequests = companyJoinRequestRepository
                .findByRecruiterIdAndStatusAndIdNot(recruiterId, StatusEnum.PENDING, approvedRequestId);

        for (CompanyJoinRequest pendingRequest : pendingRequests) {
            pendingRequest.setStatus(StatusEnum.REJECTED);
            pendingRequest.setReviewNote("Auto rejected because another company join request was approved");
            pendingRequest.setReviewedBy(reviewer);
            pendingRequest.setReviewedAt(LocalDateTime.now());
        }
        companyJoinRequestRepository.saveAll(pendingRequests);
    }

    private CompanyJoinRequest getJoinRequestOrThrow(Long requestId) {
        return companyJoinRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Company join request not found"));
    }

    private Company getCompanyOrThrow(Long companyId) {
        return companyRepository.findByIdAndDeletedFalse(companyId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Company.ERR_NOT_FOUND));
    }

    private User getCurrentUser(UserPrincipal principal) {
        return userRepository.findById(principal.getId())
                .filter(user -> !Boolean.TRUE.equals(user.getDeleted()))
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{principal.getId()}));
    }

    private void validateCompanyCanReceiveJoinRequest(Company company) {
        if (!Boolean.TRUE.equals(company.getActive()) || !Boolean.TRUE.equals(company.getVerified())) {
            throw new BadRequestException("Only active and verified companies can receive join requests");
        }
    }

    private void validatePending(CompanyJoinRequest joinRequest) {
        if (joinRequest.getStatus() != StatusEnum.PENDING) {
            throw new BadRequestException("Company join request has already been reviewed");
        }
    }

    private void validateRecruiter(UserPrincipal principal) {
        if (!hasRole(principal, RoleConstant.RECRUITER)) {
            throw new ForbiddenException("Only recruiters can create or view company join requests");
        }
    }

    private void validateCanReviewCompanyRequests(Company company, UserPrincipal principal) {
        if (hasRole(principal, RoleConstant.ADMIN)) {
            return;
        }

        if (company.getOwner() != null && principal.getId().equals(company.getOwner().getId())) {
            return;
        }

        throw new ForbiddenException("Only admin or company owner can review company join requests");
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

    private PaginationResponseDto<CompanyJoinRequestResponse> toPageResponse(Page<CompanyJoinRequest> page) {
        PagingMeta meta = new PagingMeta(
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber() + 1,
                page.getSize(),
                "createdDate",
                "DESC"
        );

        List<CompanyJoinRequestResponse> items = page.getContent().stream()
                .map(this::toResponse)
                .toList();
        return new PaginationResponseDto<>(meta, items);
    }

    private CompanyJoinRequestResponse toResponse(CompanyJoinRequest joinRequest) {
        User recruiter = joinRequest.getRecruiter();
        Company company = joinRequest.getCompany();
        User reviewer = joinRequest.getReviewedBy();

        return CompanyJoinRequestResponse.builder()
                .id(joinRequest.getId())
                .status(joinRequest.getStatus())
                .message(joinRequest.getMessage())
                .reviewNote(joinRequest.getReviewNote())
                .reviewedAt(joinRequest.getReviewedAt())
                .createdDate(joinRequest.getCreatedDate())
                .recruiter(toRecruiterSummary(recruiter))
                .company(toCompanySummary(company))
                .reviewedBy(toReviewerSummary(reviewer))
                .build();
    }

    private CompanyJoinRequestResponse.RecruiterSummary toRecruiterSummary(User user) {
        if (user == null) {
            return null;
        }
        return CompanyJoinRequestResponse.RecruiterSummary.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }

    private CompanyJoinRequestResponse.CompanySummary toCompanySummary(Company company) {
        if (company == null) {
            return null;
        }
        return CompanyJoinRequestResponse.CompanySummary.builder()
                .id(company.getId())
                .name(company.getName())
                .active(company.getActive())
                .verified(company.getVerified())
                .build();
    }

    private CompanyJoinRequestResponse.ReviewerSummary toReviewerSummary(User user) {
        if (user == null) {
            return null;
        }
        return CompanyJoinRequestResponse.ReviewerSummary.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
