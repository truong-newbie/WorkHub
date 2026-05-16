package org.example.workhub.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.workhub.constant.ErrorMessage;
import org.example.workhub.constant.RoleConstant;
import org.example.workhub.domain.dto.pagination.PaginationResponseDto;
import org.example.workhub.domain.dto.pagination.PagingMeta;
import org.example.workhub.domain.dto.request.ResumeSearchRequest;
import org.example.workhub.domain.dto.request.ResumeUpdateRequest;
import org.example.workhub.domain.dto.request.ResumeUploadRequest;
import org.example.workhub.domain.dto.response.ResumeDownloadResponse;
import org.example.workhub.domain.dto.response.ResumeResponse;
import org.example.workhub.domain.entity.Resume;
import org.example.workhub.domain.entity.Skill;
import org.example.workhub.domain.entity.User;
import org.example.workhub.domain.mapper.ResumeMapper;
import org.example.workhub.domain.specification.ResumeSpecification;
import org.example.workhub.exception.BadRequestException;
import org.example.workhub.exception.ConflictException;
import org.example.workhub.exception.ForbiddenException;
import org.example.workhub.exception.NotFoundException;
import org.example.workhub.repository.JobApplicationRepository;
import org.example.workhub.repository.JobRepository;
import org.example.workhub.repository.ResumeRepository;
import org.example.workhub.repository.SkillRepository;
import org.example.workhub.repository.UserRepository;
import org.example.workhub.security.UserPrincipal;
import org.example.workhub.service.ResumeService;
import org.example.workhub.util.UploadFileUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ResumeServiceImpl implements ResumeService {

    private static final long MAX_FILE_SIZE = 10L * 1024L * 1024L;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "doc", "docx");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/octet-stream"
    );
    private static final Set<String> SORT_FIELDS = Set.of("id", "title", "atsScore", "uploadedAt", "isDefault", "isPublic", "createdDate", "lastModifiedDate");

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final JobRepository jobRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final ResumeMapper resumeMapper;
    private final UploadFileUtil uploadFileUtil;

    @Override
    public ResumeResponse uploadResume(ResumeUploadRequest request) {
        UserPrincipal currentUser = getCurrentUserPrincipal();
        User user = getUserFromPrincipal(currentUser);

        validateResumeFile(request.getFile());
        validateDuplicateTitle(user.getId(), request.getTitle(), null);

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultResumes(user.getId());
        }

        String fileUrl = uploadFileUtil.uploadFile(request.getFile());
        String fileName = StringUtils.cleanPath(request.getFile().getOriginalFilename());
        String fileType = getFileExtension(fileName);

        Resume resume = new Resume();
        resume.setTitle(request.getTitle().trim());
        resume.setEmail(user.getEmail());
        resume.setFileName(fileName);
        resume.setFileUrl(fileUrl);
        resume.setUrl(fileUrl);
        resume.setFileType(fileType);
        resume.setFileSize(request.getFile().getSize());
        resume.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()));
        resume.setIsPublic(Boolean.TRUE.equals(request.getIsPublic()));
        resume.setDeleted(false);
        resume.setSummary(request.getSummary());
        resume.setUploadedAt(LocalDateTime.now());
        resume.setUser(user);
        resume.setSkills(resolveSkills(request.getSkillIds()));

        Resume saved = resumeRepository.save(resume);
        return resumeMapper.toResponse(saved);
    }

    @Override
    public ResumeResponse updateResume(Long id, ResumeUpdateRequest request) {
        UserPrincipal currentUser = getCurrentUserPrincipal();
        Resume resume = findResumeById(id);
        validateManagePermission(resume, currentUser);

        if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
            validateDuplicateTitle(resume.getUser().getId(), request.getTitle(), resume.getId());
            resume.setTitle(request.getTitle().trim());
        }
        if (request.getSummary() != null) {
            resume.setSummary(request.getSummary());
        }
        if (request.getIsPublic() != null) {
            resume.setIsPublic(request.getIsPublic());
        }
        if (request.getSkillIds() != null) {
            resume.setSkills(resolveSkills(request.getSkillIds()));
        }
        if (request.getIsDefault() != null) {
            if (Boolean.TRUE.equals(request.getIsDefault())) {
                clearDefaultResumes(resume.getUser().getId());
            }
            resume.setIsDefault(request.getIsDefault());
        }
        if (request.getAtsScore() != null || request.getParsedContent() != null) {
            validateAdmin(currentUser);
            resume.setAtsScore(request.getAtsScore());
            resume.setParsedContent(request.getParsedContent());
        }

        return resumeMapper.toResponse(resumeRepository.save(resume));
    }

    @Override
    public ResumeResponse updateResumeFile(Long id, MultipartFile file) {
        UserPrincipal currentUser = getCurrentUserPrincipal();
        Resume resume = findResumeById(id);
        validateManagePermission(resume, currentUser);
        validateResumeFile(file);

        String fileUrl = uploadFileUtil.uploadFile(file);
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        resume.setFileName(fileName);
        resume.setFileUrl(fileUrl);
        resume.setUrl(fileUrl);
        resume.setFileType(getFileExtension(fileName));
        resume.setFileSize(file.getSize());
        resume.setUploadedAt(LocalDateTime.now());
        resume.setParsedContent(null);
        resume.setAtsScore(null);

        return resumeMapper.toResponse(resumeRepository.save(resume));
    }

    @Override
    public ResumeResponse deleteResume(Long id) {
        UserPrincipal currentUser = getCurrentUserPrincipal();
        Resume resume = findResumeById(id);
        validateManagePermission(resume, currentUser);

        resume.setDeleted(true);
        resume.setIsDefault(false);
        return resumeMapper.toResponse(resumeRepository.save(resume));
    }

    @Override
    @Transactional(readOnly = true)
    public ResumeResponse getResumeDetail(Long id) {
        UserPrincipal currentUser = getCurrentUserPrincipal();
        Resume resume = findResumeById(id);
        validateReadPermission(resume, currentUser);
        return resumeMapper.toResponse(resume);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponseDto<ResumeResponse> getMyResumes(ResumeSearchRequest request) {
        UserPrincipal currentUser = getCurrentUserPrincipal();
        Pageable pageable = buildPageable(request);
        Specification<Resume> spec = ResumeSpecification.withFilters(request)
                .and(ResumeSpecification.ownedBy(currentUser.getId()));

        Page<Resume> page = resumeRepository.findAll(spec, pageable);
        return toPagination(page, request);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponseDto<ResumeResponse> getAllResumes(ResumeSearchRequest request) {
        validateAdmin(getCurrentUserPrincipal());
        Pageable pageable = buildPageable(request);
        Page<Resume> page = resumeRepository.findAll(ResumeSpecification.withFilters(request), pageable);
        return toPagination(page, request);
    }

    @Override
    public ResumeResponse setDefaultResume(Long id) {
        UserPrincipal currentUser = getCurrentUserPrincipal();
        Resume resume = findResumeById(id);
        validateManagePermission(resume, currentUser);

        clearDefaultResumes(resume.getUser().getId());
        resume.setIsDefault(true);
        return resumeMapper.toResponse(resumeRepository.save(resume));
    }

    @Override
    @Transactional(readOnly = true)
    public ResumeResponse getCandidateResumeForJob(Long jobId, String candidateId) {
        Resume resume = findRecruiterAccessibleResume(jobId, candidateId);
        return resumeMapper.toResponse(resume);
    }

    @Override
    @Transactional(readOnly = true)
    public ResumeDownloadResponse downloadMyResume(Long id) {
        UserPrincipal currentUser = getCurrentUserPrincipal();
        Resume resume = findResumeById(id);
        validateReadPermission(resume, currentUser);
        return resumeMapper.toDownloadResponse(resume);
    }

    @Override
    @Transactional(readOnly = true)
    public ResumeDownloadResponse downloadCandidateResumeForJob(Long jobId, String candidateId) {
        Resume resume = findRecruiterAccessibleResume(jobId, candidateId);
        return resumeMapper.toDownloadResponse(resume);
    }

    private Resume findRecruiterAccessibleResume(Long jobId, String candidateId) {
        UserPrincipal currentUser = getCurrentUserPrincipal();
        validateRecruiterOrAdmin(currentUser);
        validateJobAccess(jobId, currentUser);

        if (!jobApplicationRepository.existsByJobIdAndUserIdAndDeletedFalse(jobId, candidateId)) {
            throw new ForbiddenException(ErrorMessage.Resume.ERR_PERMISSION_DENIED);
        }

        return resumeRepository.findShareableByCandidateId(candidateId).stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Resume.ERR_NOT_FOUND));
    }

    private Resume findResumeById(Long id) {
        return resumeRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Resume.ERR_NOT_FOUND));
    }

    private void validateResumeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException(ErrorMessage.Resume.ERR_UPLOAD_EMPTY);
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException(ErrorMessage.Resume.ERR_FILE_TOO_LARGE);
        }
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = getFileExtension(fileName);
        String contentType = file.getContentType();
        if (!ALLOWED_EXTENSIONS.contains(extension) || contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BadRequestException(ErrorMessage.Resume.ERR_FILE_INVALID);
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            throw new BadRequestException(ErrorMessage.Resume.ERR_FILE_INVALID);
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    private void validateDuplicateTitle(String userId, String title, Long currentResumeId) {
        if (currentResumeId == null) {
            resumeRepository.findByUserIdAndTitleIgnoreCase(userId, title.trim())
                    .ifPresent(resume -> {
                        throw new ConflictException(ErrorMessage.Resume.ERR_DUPLICATE_TITLE);
                    });
            return;
        }
        if (resumeRepository.existsByUserIdAndTitleIgnoreCaseAndIdNot(userId, title.trim(), currentResumeId)) {
            throw new ConflictException(ErrorMessage.Resume.ERR_DUPLICATE_TITLE);
        }
    }

    private void clearDefaultResumes(String userId) {
        List<Resume> defaults = resumeRepository.findDefaultsByUserId(userId);
        defaults.forEach(resume -> resume.setIsDefault(false));
        resumeRepository.saveAll(defaults);
    }

    private List<Skill> resolveSkills(List<Long> skillIds) {
        if (skillIds == null || skillIds.isEmpty()) {
            return List.of();
        }
        List<Skill> skills = skillRepository.findAllById(skillIds);
        if (skills.size() != skillIds.stream().distinct().count()) {
            throw new NotFoundException(ErrorMessage.Skill.ERR_NOT_FOUND);
        }
        return skills;
    }

    private void validateReadPermission(Resume resume, UserPrincipal currentUser) {
        if (isAdmin(currentUser)) {
            return;
        }
        if (isOwner(resume, currentUser)) {
            return;
        }
        if (Boolean.TRUE.equals(resume.getIsPublic())) {
            return;
        }
        throw new ForbiddenException(ErrorMessage.Resume.ERR_PERMISSION_DENIED);
    }

    private void validateManagePermission(Resume resume, UserPrincipal currentUser) {
        if (isAdmin(currentUser) || isOwner(resume, currentUser)) {
            return;
        }
        throw new ForbiddenException(ErrorMessage.Resume.ERR_PERMISSION_DENIED);
    }

    private void validateJobAccess(Long jobId, UserPrincipal currentUser) {
        if (isAdmin(currentUser)) {
            return;
        }
        jobRepository.findByIdNotDeleted(jobId)
                .filter(job -> job.getRecruiter() != null && currentUser.getId().equals(job.getRecruiter().getId()))
                .orElseThrow(() -> new ForbiddenException(ErrorMessage.Resume.ERR_PERMISSION_DENIED));
    }

    private void validateRecruiterOrAdmin(UserPrincipal currentUser) {
        if (isAdmin(currentUser) || hasRole(currentUser, RoleConstant.RECRUITER)) {
            return;
        }
        throw new ForbiddenException(ErrorMessage.Resume.ERR_PERMISSION_DENIED);
    }

    private void validateAdmin(UserPrincipal currentUser) {
        if (!isAdmin(currentUser)) {
            throw new ForbiddenException(ErrorMessage.Resume.ERR_PERMISSION_DENIED);
        }
    }

    private boolean isOwner(Resume resume, UserPrincipal currentUser) {
        return resume.getUser() != null && currentUser.getId().equals(resume.getUser().getId());
    }

    private boolean isAdmin(UserPrincipal currentUser) {
        return hasRole(currentUser, RoleConstant.ADMIN);
    }

    private boolean hasRole(UserPrincipal currentUser, String role) {
        return currentUser.getAuthorities() != null && currentUser.getAuthorities().stream()
                .anyMatch(authority -> role.equals(authority.getAuthority()));
    }

    private UserPrincipal getCurrentUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            throw new ForbiddenException(ErrorMessage.UNAUTHORIZED);
        }
        return (UserPrincipal) authentication.getPrincipal();
    }

    private User getUserFromPrincipal(UserPrincipal principal) {
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{principal.getId()}));
    }

    private Pageable buildPageable(ResumeSearchRequest request) {
        int page = request.getPage() == null || request.getPage() < 0 ? 0 : request.getPage();
        int size = request.getSize() == null || request.getSize() < 1 ? 10 : Math.min(request.getSize(), 100);
        String sortBy = SORT_FIELDS.contains(request.getSortBy()) ? request.getSortBy() : "uploadedAt";
        Sort sort = "ASC".equalsIgnoreCase(request.getSortDir()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        return PageRequest.of(page, size, sort);
    }

    private PaginationResponseDto<ResumeResponse> toPagination(Page<Resume> page, ResumeSearchRequest request) {
        PagingMeta pagingMeta = new PagingMeta(
                page.getTotalElements(),
                page.getTotalPages(),
                request.getPage() == null ? 1 : request.getPage() + 1,
                request.getSize() == null ? 10 : request.getSize(),
                SORT_FIELDS.contains(request.getSortBy()) ? request.getSortBy() : "uploadedAt",
                request.getSortDir() == null ? "DESC" : request.getSortDir()
        );
        return new PaginationResponseDto<>(pagingMeta, resumeMapper.toResponses(page.getContent()));
    }
}
