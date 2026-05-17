package org.example.workhub.service.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.workhub.base.VsResponseUtil;
import org.example.workhub.constant.ErrorMessage;
import org.example.workhub.constant.GenderEnum;
import org.example.workhub.constant.RoleConstant;
import org.example.workhub.domain.dto.pagination.PagingMeta;
import org.example.workhub.domain.dto.pagination.PaginationResponseDto;
import org.example.workhub.domain.dto.request.*;
import org.example.workhub.domain.dto.response.UserResponse;
import org.example.workhub.domain.dto.response.UserStatisticsResponse;
import org.example.workhub.domain.entity.Company;
import org.example.workhub.domain.entity.Role;
import org.example.workhub.domain.entity.User;
import org.example.workhub.domain.mapper.UserMapper;
import org.example.workhub.domain.specification.UserSpecification;
import org.example.workhub.exception.*;
import org.example.workhub.repository.CompanyRepository;
import org.example.workhub.repository.RoleRepository;
import org.example.workhub.repository.UserRepository;
import org.example.workhub.security.UserPrincipal;
import org.example.workhub.service.UserService;
import org.example.workhub.util.PaginationUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CompanyRepository companyRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    // ========== CRUD ==========

    @Override
    public UserResponse createUser(UserCreateRequest request) {
        // Validate email unique
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException(ErrorMessage.Auth.ERR_ALREADY_EXISTS_EMAIL);
        }

        // Validate username unique
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException(ErrorMessage.User.ERR_ALREADY_EXISTS_USERNAME);
        }

        // Find role
        Role role = roleRepository.findByName(request.getRoleName())
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Role.ERR_NOT_FOUND));

        // Build user entity
        User user = userMapper.toUser(request);

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);

        // Set company if provided (for RECRUITER)
        if (request.getCompanyId() != null && !request.getCompanyId().isEmpty()) {
            Long companyIdLong = Long.parseLong(request.getCompanyId());
            Company company = companyRepository.findById(companyIdLong)
                    .orElseThrow(() -> new NotFoundException(ErrorMessage.Company.ERR_NOT_FOUND));
            user.setCompany(company);
        }

        User savedUser = userRepository.save(user);
        return userMapper.toUserResponse(savedUser);
    }

    @Override
    public UserResponse updateUser(String id, UserUpdateRequest request) {
        User user = findUserByIdNotDeleted(id);
        boolean currentUserIsAdmin = isCurrentUserAdmin();

        // Validate email unique (excluding current user)
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
                throw new ConflictException(ErrorMessage.Auth.ERR_ALREADY_EXISTS_EMAIL);
            }
            user.setEmail(request.getEmail());
        }

        // Validate username unique (excluding current user)
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsernameAndIdNot(request.getUsername(), id)) {
                throw new ConflictException("Username already exists");
            }
        }

        // Update fields
        userMapper.updateUserFromRequest(request, user);

        // Update company if provided
        if (request.getCompanyId() != null) {
            if (!currentUserIsAdmin) {
                throw new ForbiddenException("Only admin can assign company directly. Recruiters must use company join requests.");
            }
            if (request.getCompanyId().isEmpty()) {
                user.setCompany(null);
            } else {
                Long companyIdLong = Long.parseLong(request.getCompanyId());
                Company company = companyRepository.findById(companyIdLong)
                        .orElseThrow(() -> new NotFoundException(ErrorMessage.Company.ERR_NOT_FOUND));
                user.setCompany(company);
            }
        }

        User updatedUser = userRepository.save(user);
        return userMapper.toUserResponse(updatedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(String id) {
        User user = findUserByIdNotDeleted(id);
        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponseDto<UserResponse> getAllUsers(UserFilterRequest filter) {
        Pageable pageable = buildPageable(filter);

        Specification<User> spec = UserSpecification.search(filter.getKeyword())
                .and(UserSpecification.withFilters(filter));

        Page<User> userPage = userRepository.findAll(spec, pageable);

        List<UserResponse> userResponses = userMapper.toUserResponses(userPage.getContent());

        PagingMeta pagingMeta = new PagingMeta(
                userPage.getTotalElements(),
                userPage.getTotalPages(),
                filter.getPage() + 1,
                filter.getSize(),
                filter.getSortBy() != null ? filter.getSortBy() : "createdDate",
                filter.getSortDir() != null ? filter.getSortDir() : "DESC"
        );

        return new PaginationResponseDto<>(pagingMeta, userResponses);
    }

    @Override
    public void deleteUser(String id) {
        User user = findUserByIdNotDeleted(id);
        user.setDeleted(true);
        user.setEnabled(false);
        userRepository.save(user);
    }

    // ========== Profile APIs ==========

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUserProfile() {
        UserPrincipal currentUser = getCurrentUserPrincipal();
        User user = userRepository.findByUsernameAndDeletedFalse(currentUser.getUsername())
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_USERNAME,
                        new String[]{currentUser.getUsername()}));
        return userMapper.toUserResponse(user);
    }

    @Override
    public UserResponse updateCurrentUserProfile(UserProfileUpdateRequest request) {
        UserPrincipal currentUser = getCurrentUserPrincipal();
        User user = userRepository.findByUsernameAndDeletedFalse(currentUser.getUsername())
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_USERNAME,
                        new String[]{currentUser.getUsername()}));

        // Validate username unique (excluding current user)
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsernameAndIdNot(request.getUsername(), user.getId())) {
                throw new ConflictException("Username already exists");
            }
        }

        userMapper.updateProfileFromRequest(request, user);
        User updatedUser = userRepository.save(user);
        return userMapper.toUserResponse(updatedUser);
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {
        UserPrincipal currentUser = getCurrentUserPrincipal();
        User user = userRepository.findByUsernameAndDeletedFalse(currentUser.getUsername())
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_USERNAME,
                        new String[]{currentUser.getUsername()}));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        // Verify new password and confirm password match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException(ErrorMessage.INVALID_REPEAT_PASSWORD);
        }

        // Encode and save new password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public UserResponse uploadAvatar(String avatarUrl) {
        UserPrincipal currentUser = getCurrentUserPrincipal();
        User user = userRepository.findByUsernameAndDeletedFalse(currentUser.getUsername())
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_USERNAME,
                        new String[]{currentUser.getUsername()}));

        user.setAvatar(avatarUrl);
        User updatedUser = userRepository.save(user);
        return userMapper.toUserResponse(updatedUser);
    }

    // ========== Admin APIs ==========

    @Override
    public void lockUser(String id, UserStatusRequest request) {
        User user = findUserByIdNotDeleted(id);
        user.setEnabled(false);
        userRepository.save(user);
    }

    @Override
    public void unlockUser(String id, UserStatusRequest request) {
        User user = findUserByIdNotDeleted(id);
        user.setEnabled(true);
        userRepository.save(user);
    }

    @Override
    public UserResponse changeUserRole(String id, UserStatusRequest request) {
        if (request.getNewRole() == null || request.getNewRole().isEmpty()) {
            throw new BadRequestException("New role is required");
        }

        User user = findUserByIdNotDeleted(id);

        Role newRole = roleRepository.findByName(request.getNewRole())
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Role.ERR_NOT_FOUND));

        user.setRole(newRole);
        User updatedUser = userRepository.save(user);
        return userMapper.toUserResponse(updatedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserStatisticsResponse getUserStatistics() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.findAll().stream()
                .filter(u -> Boolean.TRUE.equals(u.getEnabled()) && !Boolean.TRUE.equals(u.getDeleted()))
                .count();
        long lockedUsers = userRepository.findAll().stream()
                .filter(u -> !Boolean.TRUE.equals(u.getEnabled()) && !Boolean.TRUE.equals(u.getDeleted()))
                .count();
        long deletedUsers = userRepository.findAll().stream()
                .filter(u -> Boolean.TRUE.equals(u.getDeleted()))
                .count();

        // Users by role
        Map<String, Long> usersByRole = new HashMap<>();
        userRepository.findAll().stream()
                .filter(u -> !Boolean.TRUE.equals(u.getDeleted()))
                .forEach(u -> {
                    String roleName = u.getRole() != null ? u.getRole().getName() : "NONE";
                    usersByRole.merge(roleName, 1L, Long::sum);
                });

        // Users by gender
        Map<String, Long> usersByGender = new HashMap<>();
        userRepository.findAll().stream()
                .filter(u -> !Boolean.TRUE.equals(u.getDeleted()))
                .forEach(u -> {
                    String gender = u.getGender() != null ? u.getGender().name() : "UNKNOWN";
                    usersByGender.merge(gender, 1L, Long::sum);
                });

        // New users this month
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        long newUsersThisMonth = userRepository.findAll().stream()
                .filter(u -> u.getCreatedDate() != null && u.getCreatedDate().isAfter(startOfMonth))
                .count();

        // New users today
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        long newUsersToday = userRepository.findAll().stream()
                .filter(u -> u.getCreatedDate() != null && u.getCreatedDate().isAfter(startOfDay))
                .count();

        return UserStatisticsResponse.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .lockedUsers(lockedUsers)
                .deletedUsers(deletedUsers)
                .usersByRole(usersByRole)
                .usersByGender(usersByGender)
                .newUsersThisMonth(newUsersThisMonth)
                .newUsersToday(newUsersToday)
                .build();
    }

    // ========== Search APIs ==========

    @Override
    @Transactional(readOnly = true)
    public PaginationResponseDto<UserResponse> searchUsers(Specification<User> specification, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<User> userPage = userRepository.findAll(specification, pageable);

        List<UserResponse> userResponses = userMapper.toUserResponses(userPage.getContent());

        PagingMeta pagingMeta = new PagingMeta(
                userPage.getTotalElements(),
                userPage.getTotalPages(),
                page + 1,
                size,
                "createdDate",
                "DESC"
        );

        return new PaginationResponseDto<>(pagingMeta, userResponses);
    }

    // ========== Internal ==========

    @Override
    @Transactional(readOnly = true)
    public User findById(String id) {
        return findUserByIdNotDeleted(id);
    }

    // ========== Helper Methods ==========

    private User findUserByIdNotDeleted(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{id}));

        if (Boolean.TRUE.equals(user.getDeleted())) {
            throw new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{id});
        }

        return user;
    }

    private UserPrincipal getCurrentUserPrincipal() {
        org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        org.springframework.security.core.Authentication authentication =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException(ErrorMessage.UNAUTHORIZED);
        }

        return (UserPrincipal) authentication.getPrincipal();
    }

    private boolean isCurrentUserAdmin() {
        org.springframework.security.core.Authentication authentication =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> RoleConstant.ADMIN.equals(authority.getAuthority()));
    }

    private Pageable buildPageable(UserFilterRequest filter) {
        Sort sort = "ASC".equalsIgnoreCase(filter.getSortDir())
                ? Sort.by(filter.getSortBy()).ascending()
                : Sort.by(filter.getSortBy()).descending();
        return PageRequest.of(filter.getPage(), filter.getSize(), sort);
    }
}
