package org.example.workhub.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.example.workhub.constant.ErrorMessage;
import org.example.workhub.constant.RoleConstant;
import org.example.workhub.domain.dto.pagination.PaginationResponseDto;
import org.example.workhub.domain.dto.pagination.PagingMeta;
import org.example.workhub.domain.dto.request.SkillRequestDto;
import org.example.workhub.domain.dto.request.SkillSearchRequest;
import org.example.workhub.domain.dto.response.PopularSkillResponse;
import org.example.workhub.domain.dto.response.SkillResponseDto;
import org.example.workhub.domain.dto.response.SkillSuggestionResponse;
import org.example.workhub.domain.dto.specification.SkillSpecification;
import org.example.workhub.domain.entity.Skill;
import org.example.workhub.domain.mapper.SkillMapper;
import org.example.workhub.exception.BadRequestException;
import org.example.workhub.exception.ConflictException;
import org.example.workhub.exception.NotFoundException;
import org.example.workhub.repository.SkillRepository;
import org.example.workhub.security.UserPrincipal;
import org.example.workhub.service.SkillService;
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

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Log4j2
@Transactional
public class SkillServiceImpl implements SkillService {

    SkillRepository skillRepository;
    SkillMapper skillMapper;

    @Override
    public SkillResponseDto create(SkillRequestDto request) {
        String normalizedName = normalizeName(request.getName());
        validateDuplicateName(normalizedName, null);

        Skill skill = skillMapper.toEntity(request);
        skill.setName(normalizedName);
        skill.setSlug(generateUniqueSlug(normalizedName, null));
        skill.setActive(request.getActive() == null || request.getActive());
        return skillMapper.toDto(skillRepository.save(skill));
    }

    @Override
    public SkillResponseDto update(Long id, SkillRequestDto request) {
        Skill skill = findByIdNotDeleted(id);
        String normalizedName = normalizeName(request.getName());
        validateDuplicateName(normalizedName, id);

        skill.setName(normalizedName);
        skill.setSlug(generateUniqueSlug(normalizedName, id));
        skill.setLevel(trimToNull(request.getLevel()));
        skill.setDescription(trimToNull(request.getDescription()));
        if (request.getActive() != null) {
            skill.setActive(request.getActive());
        }
        return skillMapper.toDto(skillRepository.save(skill));
    }

    @Override
    @Transactional(readOnly = true)
    public SkillResponseDto getById(Long id) {
        Skill skill = findByIdNotDeleted(id);
        if (!isAdmin(getOptionalCurrentUserPrincipal()) && !Boolean.TRUE.equals(skill.getActive())) {
            throw new NotFoundException(ErrorMessage.Skill.ERR_NOT_FOUND);
        }
        return skillMapper.toDto(skill);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponseDto<SkillResponseDto> getAll(SkillSearchRequest request) {
        boolean admin = isAdmin(getOptionalCurrentUserPrincipal());
        Pageable pageable = buildPageable(request);
        Specification<Skill> spec = SkillSpecification.withFilters(request, admin);

        if (!admin) {
            spec = spec.and(SkillSpecification.publicVisible());
        }

        Page<Skill> page = skillRepository.findAll(spec, pageable);
        List<SkillResponseDto> items = page.getContent().stream()
                .map(skillMapper::toDto)
                .toList();

        return new PaginationResponseDto<>(buildMeta(page, request), items);
    }

    @Override
    public SkillResponseDto enable(Long id) {
        Skill skill = findByIdNotDeleted(id);
        if (Boolean.TRUE.equals(skill.getActive())) {
            throw new BadRequestException(ErrorMessage.Skill.ERR_ALREADY_ENABLED);
        }
        skill.setActive(true);
        return skillMapper.toDto(skillRepository.save(skill));
    }

    @Override
    public SkillResponseDto disable(Long id) {
        Skill skill = findByIdNotDeleted(id);
        if (Boolean.FALSE.equals(skill.getActive())) {
            throw new BadRequestException(ErrorMessage.Skill.ERR_ALREADY_DISABLED);
        }
        skill.setActive(false);
        return skillMapper.toDto(skillRepository.save(skill));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SkillSuggestionResponse> getSuggestions(String keyword, int limit) {
        int safeLimit = normalizeLimit(limit);
        String search = keyword == null ? "" : keyword.trim();
        return skillRepository.findActiveSuggestions(search, PageRequest.of(0, safeLimit)).stream()
                .map(skillMapper::toSuggestion)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PopularSkillResponse> getPopularSkills(int limit) {
        int safeLimit = normalizeLimit(limit);
        return skillRepository.findPopularActiveSkills(PageRequest.of(0, safeLimit)).stream()
                .map(row -> skillMapper.toPopular((Skill) row[0], (Long) row[1]))
                .toList();
    }

    @Override
    public void delete(Long id) {
        Skill skill = findByIdNotDeleted(id);
        if (isSkillInUse(id)) {
            throw new ConflictException(ErrorMessage.Skill.ERR_IN_USE);
        }
        skill.setDeleted(true);
        skill.setActive(false);
        skillRepository.save(skill);
    }

    private Skill findByIdNotDeleted(Long id) {
        return skillRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Skill.ERR_NOT_FOUND));
    }

    private void validateDuplicateName(String name, Long currentId) {
        boolean duplicated = currentId == null
                ? skillRepository.existsByNameIgnoreCaseAndDeletedFalse(name)
                : skillRepository.existsByNameIgnoreCaseAndDeletedFalseAndIdNot(name, currentId);
        if (duplicated) {
            throw new ConflictException(ErrorMessage.Skill.ERR_ALREADY_EXISTS_SKILL);
        }
    }

    private boolean isSkillInUse(Long id) {
        return skillRepository.countJobsUsingSkill(id) > 0
                || skillRepository.countResumesUsingSkill(id) > 0
                || skillRepository.countSubscribersUsingSkill(id) > 0;
    }

    private String generateUniqueSlug(String name, Long currentId) {
        String baseSlug = toSlug(name);
        String slug = baseSlug;
        int counter = 1;
        while (currentId == null
                ? skillRepository.existsBySlugAndDeletedFalse(slug)
                : skillRepository.existsBySlugAndDeletedFalseAndIdNot(slug, currentId)) {
            slug = baseSlug + "-" + counter++;
        }
        return slug;
    }

    private String normalizeName(String name) {
        return name.trim().replaceAll("\\s+", " ");
    }

    private String toSlug(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");
        return normalized.isBlank() ? "skill" : normalized;
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private int normalizeLimit(int limit) {
        if (limit < 1) {
            return 10;
        }
        return Math.min(limit, 50);
    }

    private Pageable buildPageable(SkillSearchRequest request) {
        String sortBy = normalizeSortBy(request.getSortBy());
        Sort.Direction direction = Boolean.TRUE.equals(request.getIsAscending()) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(request.getPageNum(), request.getPageSize(), direction, sortBy);
    }

    private String normalizeSortBy(String sortBy) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return "createdDate";
        }
        return switch (sortBy.trim()) {
            case "id", "name", "slug", "level", "active", "deleted", "createdDate", "lastModifiedDate" -> sortBy.trim();
            default -> "createdDate";
        };
    }

    private PagingMeta buildMeta(Page<Skill> page, SkillSearchRequest request) {
        String sortBy = normalizeSortBy(request.getSortBy());
        return new PagingMeta(
                page.getTotalElements(),
                page.getTotalPages(),
                request.getPageNum() + 1,
                request.getPageSize(),
                sortBy,
                Boolean.TRUE.equals(request.getIsAscending()) ? "ASC" : "DESC"
        );
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

    private boolean isAdmin(Optional<UserPrincipal> currentUser) {
        return currentUser.map(user -> user.getAuthorities() != null
                && user.getAuthorities().stream().anyMatch(authority -> RoleConstant.ADMIN.equals(authority.getAuthority())))
                .orElse(false);
    }
}
