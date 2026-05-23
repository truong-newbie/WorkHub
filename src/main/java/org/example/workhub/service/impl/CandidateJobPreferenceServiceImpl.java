package org.example.workhub.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.workhub.constant.ErrorMessage;
import org.example.workhub.domain.dto.request.CandidateJobPreferenceCreateRequest;
import org.example.workhub.domain.dto.request.CandidateJobPreferenceUpdateRequest;
import org.example.workhub.domain.dto.response.CandidateJobPreferenceResponse;
import org.example.workhub.domain.dto.response.CandidateOnboardingStatusResponse;
import org.example.workhub.domain.entity.CandidateJobPreference;
import org.example.workhub.domain.entity.Skill;
import org.example.workhub.domain.entity.User;
import org.example.workhub.domain.mapper.CandidateJobPreferenceMapper;
import org.example.workhub.exception.BadRequestException;
import org.example.workhub.exception.ConflictException;
import org.example.workhub.exception.NotFoundException;
import org.example.workhub.repository.CandidateJobPreferenceRepository;
import org.example.workhub.repository.SkillRepository;
import org.example.workhub.repository.UserRepository;
import org.example.workhub.security.UserPrincipal;
import org.example.workhub.service.CandidateJobPreferenceService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class CandidateJobPreferenceServiceImpl implements CandidateJobPreferenceService {

    CandidateJobPreferenceRepository candidateJobPreferenceRepository;
    SkillRepository skillRepository;
    UserRepository userRepository;
    CandidateJobPreferenceMapper candidateJobPreferenceMapper;

    @Override
    @Transactional(readOnly = true)
    public CandidateOnboardingStatusResponse getOnboardingStatus() {
        UserPrincipal principal = getCurrentUserPrincipal();
        boolean exists = candidateJobPreferenceRepository.existsByCandidateId(principal.getId());
        return new CandidateOnboardingStatusResponse(exists, !exists);
    }

    @Override
    public CandidateJobPreferenceResponse createPreference(CandidateJobPreferenceCreateRequest request) {
        User candidate = getCurrentUser();
        if (candidateJobPreferenceRepository.existsByCandidateId(candidate.getId())) {
            throw new ConflictException(ErrorMessage.CandidateJobPreference.ERR_ALREADY_EXISTS);
        }

        validateSalaryRange(request.getExpectedSalaryMin(), request.getExpectedSalaryMax());
        Set<Skill> skills = getValidActiveSkills(request.getSkillIds());

        CandidateJobPreference preference = candidateJobPreferenceMapper.toEntity(request);
        preference.setCandidate(candidate);
        preference.setSkills(skills);

        return candidateJobPreferenceMapper.toResponse(candidateJobPreferenceRepository.save(preference));
    }

    @Override
    public CandidateJobPreferenceResponse updatePreference(CandidateJobPreferenceUpdateRequest request) {
        UserPrincipal principal = getCurrentUserPrincipal();
        CandidateJobPreference preference = candidateJobPreferenceRepository.findByCandidateId(principal.getId())
                .orElseThrow(() -> new NotFoundException(ErrorMessage.CandidateJobPreference.ERR_NOT_FOUND));

        validateSalaryRange(request.getExpectedSalaryMin(), request.getExpectedSalaryMax());
        Set<Skill> skills = getValidActiveSkills(request.getSkillIds());

        candidateJobPreferenceMapper.updateEntityFromRequest(request, preference);
        preference.setSkills(skills);

        return candidateJobPreferenceMapper.toResponse(candidateJobPreferenceRepository.save(preference));
    }

    @Override
    @Transactional(readOnly = true)
    public CandidateJobPreferenceResponse getMyPreference() {
        UserPrincipal principal = getCurrentUserPrincipal();
        CandidateJobPreference preference = candidateJobPreferenceRepository.findByCandidateId(principal.getId())
                .orElseThrow(() -> new NotFoundException(ErrorMessage.CandidateJobPreference.ERR_NOT_FOUND));
        return candidateJobPreferenceMapper.toResponse(preference);
    }

    private User getCurrentUser() {
        UserPrincipal principal = getCurrentUserPrincipal();
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{principal.getId()}));
    }

    private UserPrincipal getCurrentUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken
                || !(authentication.getPrincipal() instanceof UserPrincipal principal)
                || principal.getId() == null) {
            throw new BadRequestException(ErrorMessage.UNAUTHORIZED);
        }
        return principal;
    }

    private void validateSalaryRange(BigDecimal min, BigDecimal max) {
        if (min != null && max != null && max.compareTo(min) < 0) {
            throw new BadRequestException(ErrorMessage.CandidateJobPreference.ERR_INVALID_SALARY_RANGE);
        }
    }

    private Set<Skill> getValidActiveSkills(Set<Long> skillIds) {
        Set<Long> distinctIds = new HashSet<>(skillIds);
        List<Skill> skills = skillRepository.findAllById(distinctIds);
        boolean invalid = skills.size() != distinctIds.size()
                || skills.stream().anyMatch(skill -> !Boolean.TRUE.equals(skill.getActive())
                || Boolean.TRUE.equals(skill.getDeleted()));
        if (invalid) {
            throw new NotFoundException(ErrorMessage.CandidateJobPreference.ERR_SKILL_NOT_FOUND);
        }
        return new HashSet<>(skills);
    }
}
