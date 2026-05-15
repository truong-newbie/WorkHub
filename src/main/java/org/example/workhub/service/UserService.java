package org.example.workhub.service;


import org.example.workhub.domain.dto.pagination.PaginationResponseDto;
import org.example.workhub.domain.dto.request.*;
import org.example.workhub.domain.dto.response.UserResponse;
import org.example.workhub.domain.dto.response.UserStatisticsResponse;
import org.example.workhub.domain.entity.User;
import org.springframework.data.jpa.domain.Specification;

public interface UserService {

    // ========== CRUD ==========
    UserResponse createUser(UserCreateRequest request);

    UserResponse updateUser(String id, UserUpdateRequest request);

    UserResponse getUserById(String id);

    PaginationResponseDto<UserResponse> getAllUsers(UserFilterRequest filter);

    void deleteUser(String id);

    // ========== Profile APIs ==========
    UserResponse getCurrentUserProfile();

    UserResponse updateCurrentUserProfile(UserProfileUpdateRequest request);

    void changePassword(ChangePasswordRequest request);

    UserResponse uploadAvatar(String avatarUrl);

    // ========== Admin APIs ==========
    void lockUser(String id, UserStatusRequest request);

    void unlockUser(String id, UserStatusRequest request);

    UserResponse changeUserRole(String id, UserStatusRequest request);

    UserStatisticsResponse getUserStatistics();

    // ========== Search APIs ==========
    PaginationResponseDto<UserResponse> searchUsers(Specification<User> specification, int page, int size);

    // ========== Internal ==========
    User findById(String id);
}