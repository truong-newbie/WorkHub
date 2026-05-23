package org.example.workhub.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.workhub.constant.ErrorMessage;
import org.example.workhub.constant.RoleConstant;
import org.example.workhub.domain.entity.Job;
import org.example.workhub.domain.entity.User;
import org.example.workhub.exception.ForbiddenException;
import org.example.workhub.exception.NotFoundException;
import org.example.workhub.repository.UserRepository;
import org.example.workhub.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class AssessmentSecuritySupport {

    private final UserRepository userRepository;

    UserPrincipal currentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ForbiddenException(ErrorMessage.UNAUTHORIZED);
        }
        return (UserPrincipal) authentication.getPrincipal();
    }

    User currentUser() {
        UserPrincipal principal = currentPrincipal();
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{principal.getId()}));
    }

    boolean isAdmin(UserPrincipal principal) {
        return principal.getAuthorities() != null && principal.getAuthorities().stream()
                .anyMatch(authority -> RoleConstant.ADMIN.equals(authority.getAuthority()) || "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    void validateRecruiterOwnsJob(Job job) {
        UserPrincipal principal = currentPrincipal();
        if (isAdmin(principal)) return;
        if (job.getRecruiter() != null && principal.getId().equals(job.getRecruiter().getId())) return;

        User user = currentUser();
        if (user.getCompany() != null && job.getCompany() != null && user.getCompany().getId().equals(job.getCompany().getId())) return;

        throw new ForbiddenException(ErrorMessage.Assessment.ERR_NOT_OWNER);
    }
}
