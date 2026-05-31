package org.example.workhub.service.impl;

import org.example.workhub.constant.RecruiterRequestStatus;
import org.example.workhub.constant.RoleConstant;
import org.example.workhub.domain.dto.request.RecruiterRequestCreateRequest;
import org.example.workhub.domain.dto.request.RecruiterRequestReviewRequest;
import org.example.workhub.domain.dto.response.RecruiterRequestResponse;
import org.example.workhub.domain.entity.RecruiterRequest;
import org.example.workhub.domain.entity.Role;
import org.example.workhub.domain.entity.User;
import org.example.workhub.repository.RecruiterRequestRepository;
import org.example.workhub.repository.RoleRepository;
import org.example.workhub.repository.UserRepository;
import org.example.workhub.security.UserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecruiterRequestServiceImplTest {

    @Mock
    private RecruiterRequestRepository recruiterRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RecruiterRequestServiceImpl recruiterRequestService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createStoresPendingRequestForCandidate() {
        authenticate("candidate-id", RoleConstant.CANDIDATE);
        User candidate = user("candidate-id", "candidate@example.com", RoleConstant.CANDIDATE);
        RecruiterRequestCreateRequest request = new RecruiterRequestCreateRequest();
        request.setMessage("  I want to recruit for my company.  ");

        when(userRepository.findById(candidate.getId())).thenReturn(Optional.of(candidate));
        when(recruiterRequestRepository.existsByUserIdAndStatus(
                candidate.getId(),
                RecruiterRequestStatus.PENDING)).thenReturn(false);
        when(recruiterRequestRepository.save(any(RecruiterRequest.class))).thenAnswer(invocation -> {
            RecruiterRequest saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        RecruiterRequestResponse response = recruiterRequestService.create(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo(RecruiterRequestStatus.PENDING);
        assertThat(response.getMessage()).isEqualTo("I want to recruit for my company.");
        assertThat(response.getUser().getRoleName()).isEqualTo(RoleConstant.CANDIDATE);
    }

    @Test
    void approveChangesCandidateRoleToRecruiter() {
        authenticate("admin-id", RoleConstant.ADMIN);
        User admin = user("admin-id", "admin@example.com", RoleConstant.ADMIN);
        User candidate = user("candidate-id", "candidate@example.com", RoleConstant.CANDIDATE);
        Role recruiterRole = role(RoleConstant.RECRUITER);
        RecruiterRequest recruiterRequest = new RecruiterRequest();
        recruiterRequest.setId(1L);
        recruiterRequest.setUser(candidate);
        recruiterRequest.setStatus(RecruiterRequestStatus.PENDING);
        RecruiterRequestReviewRequest review = new RecruiterRequestReviewRequest();
        review.setReviewNote("Approved");

        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(recruiterRequestRepository.findById(recruiterRequest.getId()))
                .thenReturn(Optional.of(recruiterRequest));
        when(roleRepository.findByName(RoleConstant.RECRUITER)).thenReturn(Optional.of(recruiterRole));
        when(recruiterRequestRepository.findByUserIdAndStatusAndIdNot(
                candidate.getId(),
                RecruiterRequestStatus.PENDING,
                recruiterRequest.getId())).thenReturn(List.of());
        when(recruiterRequestRepository.save(recruiterRequest)).thenReturn(recruiterRequest);

        RecruiterRequestResponse response = recruiterRequestService.approve(recruiterRequest.getId(), review);

        assertThat(candidate.getRole().getName()).isEqualTo(RoleConstant.RECRUITER);
        assertThat(response.getStatus()).isEqualTo(RecruiterRequestStatus.APPROVED);
        assertThat(response.getReviewNote()).isEqualTo("Approved");
        assertThat(response.getUser().getRoleName()).isEqualTo(RoleConstant.RECRUITER);
        verify(userRepository).save(candidate);
    }

    private void authenticate(String userId, String roleName) {
        UserPrincipal principal = new UserPrincipal(
                userId,
                userId + "@example.com",
                "password",
                List.of(new SimpleGrantedAuthority(roleName)));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    private User user(String id, String email, String roleName) {
        User user = new User();
        user.setId(id);
        user.setUsername(id);
        user.setEmail(email);
        user.setPassword("password");
        user.setRole(role(roleName));
        user.setDeleted(false);
        return user;
    }

    private Role role(String name) {
        Role role = new Role();
        role.setName(name);
        return role;
    }
}
