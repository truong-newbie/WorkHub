package org.example.workhub.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.workhub.constant.ErrorMessage;
import org.example.workhub.constant.RoleConstant;
import org.example.workhub.constant.SortByDataConstant;
import org.example.workhub.constant.StatusEnum;
import org.example.workhub.domain.dto.pagination.PaginationResponseDto;
import org.example.workhub.domain.dto.pagination.PagingMeta;
import org.example.workhub.domain.dto.request.CompanyRequestDto;
import org.example.workhub.domain.dto.request.CompanySearchRequest;
import org.example.workhub.domain.dto.response.CompanyResponseDto;
import org.example.workhub.domain.dto.response.CompanyStatisticsResponse;
import org.example.workhub.domain.dto.response.JobResponse;
import org.example.workhub.domain.entity.Company;
import org.example.workhub.domain.entity.Job;
import org.example.workhub.domain.entity.User;
import org.example.workhub.domain.mapper.CompanyMapper;
import org.example.workhub.domain.mapper.JobMapper;
import org.example.workhub.domain.specification.CompanySpecification;
import org.example.workhub.exception.BadRequestException;
import org.example.workhub.exception.ForbiddenException;
import org.example.workhub.exception.NotFoundException;
import org.example.workhub.repository.CompanyRepository;
import org.example.workhub.repository.JobRepository;
import org.example.workhub.repository.UserRepository;
import org.example.workhub.security.UserPrincipal;
import org.example.workhub.service.CompanyService;
import org.example.workhub.util.UploadFileUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.text.Normalizer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Transactional
public class CompanyServiceImpl implements CompanyService {

    CompanyRepository companyRepository;
    UserRepository userRepository;
    JobRepository jobRepository;
    CompanyMapper companyMapper;
    JobMapper jobMapper;
    UploadFileUtil uploadFileUtil;

    @Override
    @Transactional(readOnly = true)
    public PaginationResponseDto<CompanyResponseDto> getAll(CompanySearchRequest request) {
        Pageable pageable = buildPageable(request);
        Specification<Company> spec = CompanySpecification.withFilters(request);

        if (isPublicViewer(getOptionalCurrentUserPrincipal())) {
            spec = spec.and(CompanySpecification.visibleToPublic());
        }

        Page<Company> page = companyRepository.findAll(spec, pageable);
        List<CompanyResponseDto> items = companyMapper.toDtoList(page.getContent());
        return new PaginationResponseDto<>(buildMeta(page, request), items);
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyResponseDto getById(Long id) {
        Company company = getCompanyOrThrow(id);
        validateCanViewCompany(company);
        return companyMapper.toDto(company);
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyResponseDto getCurrentRecruiterCompany() {
        UserPrincipal currentUser = getCurrentUserPrincipal();
        Company company = getCurrentUserCompany(currentUser);
        return companyMapper.toDto(company);
    }

    @Override
    public CompanyResponseDto create(CompanyRequestDto request) {
        UserPrincipal currentUser = getCurrentUserPrincipal();
        validateRecruiterOrAdmin(currentUser);
        validateCompanyName(request.getName(), null);

        User owner = getUserFromPrincipal(currentUser);
        Company company = new Company();
        applyRequest(company, request);
        company.setSlug(generateUniqueSlug(request.getName(), null));
        company.setOwner(owner);
        company.setVerified(isAdmin(currentUser));
        company.setActive(request.getActive() != null ? request.getActive() : true);

        Company saved = companyRepository.save(company);
        if (isRecruiter(currentUser)) {
            owner.setCompany(saved);
            userRepository.save(owner);
        }
        return companyMapper.toDto(saved);
    }

    @Override
    public CompanyResponseDto update(Long id, CompanyRequestDto request) {
        UserPrincipal currentUser = getCurrentUserPrincipal();
        Company company = getCompanyOrThrow(id);
        validateCanManageCompany(company, currentUser);
        validateCompanyName(request.getName(), id);

        applyRequest(company, request);
        company.setSlug(generateUniqueSlug(request.getName(), id));
        if (request.getActive() != null && isAdmin(currentUser)) {
            company.setActive(request.getActive());
        }
        return companyMapper.toDto(companyRepository.save(company));
    }

    @Override
    public CompanyResponseDto updateCurrentRecruiterCompany(CompanyRequestDto request) {
        UserPrincipal currentUser = getCurrentUserPrincipal();
        Company company = getCurrentUserCompany(currentUser);
        validateCompanyName(request.getName(), company.getId());

        applyRequest(company, request);
        company.setSlug(generateUniqueSlug(request.getName(), company.getId()));
        return companyMapper.toDto(companyRepository.save(company));
    }

    @Override
    public CompanyResponseDto uploadLogo(Long id, MultipartFile file) {
        validateImageFile(file);
        UserPrincipal currentUser = getCurrentUserPrincipal();
        Company company = getCompanyOrThrow(id);
        validateCanManageCompany(company, currentUser);

        company.setLogo(uploadFileUtil.uploadFile(file));
        return companyMapper.toDto(companyRepository.save(company));
    }

    @Override
    public CompanyResponseDto uploadCover(Long id, MultipartFile file) {
        validateImageFile(file);
        UserPrincipal currentUser = getCurrentUserPrincipal();
        Company company = getCompanyOrThrow(id);
        validateCanManageCompany(company, currentUser);

        company.setCoverImage(uploadFileUtil.uploadFile(file));
        return companyMapper.toDto(companyRepository.save(company));
    }

    @Override
    public CompanyResponseDto enable(Long id) {
        Company company = getCompanyOrThrow(id);
        company.setActive(true);
        return companyMapper.toDto(companyRepository.save(company));
    }

    @Override
    public CompanyResponseDto disable(Long id) {
        Company company = getCompanyOrThrow(id);
        if (Boolean.FALSE.equals(company.getActive())) {
            throw new BadRequestException(ErrorMessage.Company.ERR_ALREADY_DISABLED);
        }
        company.setActive(false);
        return companyMapper.toDto(companyRepository.save(company));
    }

    @Override
    public CompanyResponseDto approve(Long id) {
        Company company = getCompanyOrThrow(id);
        if (Boolean.TRUE.equals(company.getVerified())) {
            throw new BadRequestException(ErrorMessage.Company.ERR_ALREADY_APPROVED);
        }
        company.setVerified(true);
        company.setActive(true);
        return companyMapper.toDto(companyRepository.save(company));
    }

    @Override
    public CompanyResponseDto reject(Long id) {
        Company company = getCompanyOrThrow(id);
        company.setVerified(false);
        return companyMapper.toDto(companyRepository.save(company));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponseDto<JobResponse> getCompanyJobs(Long id, CompanySearchRequest request) {
        Company company = getCompanyOrThrow(id);
        Optional<UserPrincipal> currentUser = getOptionalCurrentUserPrincipal();
        boolean canManage = currentUser.map(user -> canManageCompany(company, user)).orElse(false);
        if (!canManage) {
            validatePublicCompany(company);
        }

        Pageable pageable = buildPageable(request);
        Page<Job> page = canManage
                ? jobRepository.findPageByCompanyIdNotDeleted(id, pageable)
                : jobRepository.findPublicPageByCompanyId(id, Instant.now(), pageable);
        List<JobResponse> items = jobMapper.toResponses(page.getContent());
        return new PaginationResponseDto<>(buildMeta(page, request), items);
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyStatisticsResponse getCompanyStatistics(Long id) {
        UserPrincipal currentUser = getCurrentUserPrincipal();
        Company company = getCompanyOrThrow(id);
        validateCanManageCompany(company, currentUser);

        return CompanyStatisticsResponse.builder()
                .companyId(id)
                .totalJobs(jobRepository.countByCompanyIdAndDeletedFalse(id))
                .activeJobs(jobRepository.countByCompanyIdAndPublishedTrueAndDeletedFalse(id))
                .inactiveJobs(jobRepository.countByCompanyIdAndPublishedFalseAndDeletedFalse(id))
                .totalApplications(jobRepository.countApplicationsByCompanyId(id))
                .pendingApplications(jobRepository.countApplicationsByCompanyIdAndStatus(id, StatusEnum.PENDING))
                .acceptedApplications(jobRepository.countApplicationsByCompanyIdAndStatus(id, StatusEnum.APPROVED))
                .rejectedApplications(jobRepository.countApplicationsByCompanyIdAndStatus(id, StatusEnum.REJECTED))
                .build();
    }

    @Override
    public void delete(Long id) {
        UserPrincipal currentUser = getCurrentUserPrincipal();
        Company company = getCompanyOrThrow(id);
        validateCanManageCompany(company, currentUser);

        company.setDeleted(true);
        company.setActive(false);
        companyRepository.save(company);
    }

    private Company getCompanyOrThrow(Long id) {
        return companyRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Company.ERR_NOT_FOUND));
    }

    private Company getCurrentUserCompany(UserPrincipal currentUser) {
        return companyRepository.findByOwnerIdAndDeletedFalse(currentUser.getId())
                .or(() -> companyRepository.findByUserIdAndDeletedFalse(currentUser.getId()))
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Company.ERR_OWNER_NOT_FOUND));
    }

    private void applyRequest(Company company, CompanyRequestDto request) {
        company.setName(request.getName().trim());
        company.setDescription(trimToNull(request.getDescription()));
        company.setWebsite(trimToNull(request.getWebsite()));
        company.setEmail(trimToNull(request.getEmail()));
        company.setPhone(trimToNull(request.getPhone()));
        company.setAddress(trimToNull(request.getAddress()));
        company.setCity(trimToNull(request.getCity()));
        company.setCountry(trimToNull(request.getCountry()));
        company.setCompanySize(trimToNull(request.getCompanySize()));
        company.setIndustry(trimToNull(request.getIndustry()));
        company.setTaxCode(trimToNull(request.getTaxCode()));
        company.setLogo(trimToNull(request.getLogo()));
        company.setCoverImage(trimToNull(request.getCoverImage()));
    }

    private void validateCompanyName(String name, Long currentId) {
        if (currentId == null && companyRepository.existsByNameIgnoreCaseAndDeletedFalse(name.trim())) {
            throw new BadRequestException(ErrorMessage.Company.ERR_ALREADY_EXISTS_COMPANY);
        }
        if (currentId != null && companyRepository.existsByNameIgnoreCaseAndDeletedFalseAndIdNot(name.trim(), currentId)) {
            throw new BadRequestException(ErrorMessage.Company.ERR_ALREADY_EXISTS_COMPANY);
        }
    }

    private void validateCanViewCompany(Company company) {
        Optional<UserPrincipal> currentUser = getOptionalCurrentUserPrincipal();
        if (currentUser.map(user -> canManageCompany(company, user)).orElse(false)) {
            return;
        }
        validatePublicCompany(company);
    }

    private void validatePublicCompany(Company company) {
        if (Boolean.TRUE.equals(company.getDeleted())
                || !Boolean.TRUE.equals(company.getActive())
                || !Boolean.TRUE.equals(company.getVerified())) {
            throw new NotFoundException(ErrorMessage.Company.ERR_NOT_FOUND);
        }
    }

    private void validateCanManageCompany(Company company, UserPrincipal currentUser) {
        if (!canManageCompany(company, currentUser)) {
            throw new ForbiddenException(ErrorMessage.Company.ERR_PERMISSION_DENIED);
        }
    }

    private boolean canManageCompany(Company company, UserPrincipal currentUser) {
        return isAdmin(currentUser)
                || company.getOwner() != null && currentUser.getId().equals(company.getOwner().getId())
                || company.getUsers() != null && company.getUsers().stream()
                .anyMatch(user -> currentUser.getId().equals(user.getId()));
    }

    private void validateRecruiterOrAdmin(UserPrincipal currentUser) {
        if (!isRecruiter(currentUser) && !isAdmin(currentUser)) {
            throw new ForbiddenException(ErrorMessage.Company.ERR_PERMISSION_DENIED);
        }
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException(ErrorMessage.Company.ERR_FILE_EMPTY);
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException(ErrorMessage.Company.ERR_FILE_INVALID);
        }
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

    private Optional<UserPrincipal> getOptionalCurrentUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken
                || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            return Optional.empty();
        }
        return Optional.of(principal);
    }

    private User getUserFromPrincipal(UserPrincipal principal) {
        return userRepository.findById(principal.getId())
                .filter(user -> !Boolean.TRUE.equals(user.getDeleted()))
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{principal.getId()}));
    }

    private boolean isPublicViewer(Optional<UserPrincipal> currentUser) {
        return currentUser.isEmpty() || (!isAdmin(currentUser.get()) && !isRecruiter(currentUser.get()));
    }

    private boolean isAdmin(UserPrincipal currentUser) {
        return hasRole(currentUser, RoleConstant.ADMIN);
    }

    private boolean isRecruiter(UserPrincipal currentUser) {
        return hasRole(currentUser, RoleConstant.RECRUITER);
    }

    private boolean hasRole(UserPrincipal currentUser, String role) {
        return currentUser.getAuthorities() != null
                && currentUser.getAuthorities().stream().anyMatch(authority -> role.equals(authority.getAuthority()));
    }

    private String generateUniqueSlug(String name, Long currentId) {
        String baseSlug = toSlug(name);
        String slug = baseSlug;
        int counter = 1;
        while (currentId == null
                ? companyRepository.existsBySlugAndDeletedFalse(slug)
                : companyRepository.existsBySlugAndDeletedFalseAndIdNot(slug, currentId)) {
            slug = baseSlug + "-" + counter++;
        }
        return slug;
    }

    private String toSlug(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");
        return normalized.isBlank() ? "company" : normalized;
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private Pageable buildPageable(CompanySearchRequest request) {
        String sortBy = request.getSortBy(SortByDataConstant.COMPANY);
        Sort.Direction direction = Boolean.TRUE.equals(request.getIsAscending()) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(request.getPageNum(), request.getPageSize(), direction, sortBy);
    }

    private <T> PagingMeta buildMeta(Page<T> page, CompanySearchRequest request) {
        return new PagingMeta(
                page.getTotalElements(),
                page.getTotalPages(),
                request.getPageNum() + 1,
                request.getPageSize(),
                request.getSortBy(SortByDataConstant.COMPANY),
                Boolean.TRUE.equals(request.getIsAscending()) ? "ASC" : "DESC"
        );
    }
}
