package org.example.workhub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.example.workhub.base.RestApiV1;
import org.example.workhub.base.VsResponseUtil;
import org.example.workhub.constant.RoleConstant;
import org.example.workhub.constant.UrlConstant;
import org.example.workhub.domain.dto.pagination.PaginationResponseDto;
import org.example.workhub.domain.dto.request.*;
import org.example.workhub.domain.dto.response.UserResponse;
import org.example.workhub.domain.dto.response.UserStatisticsResponse;
import org.example.workhub.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Log4j2
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
@RestApiV1
@Tag(name = "User Controller", description = "APIs for user management")
public class UserController {

    UserService userService;

    // ========== CRUD APIs ==========

    @Operation(summary = "Create a new user", description = "Create a new user (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "409", description = "Email or username already exists"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping(UrlConstant.User.USER_BASE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUser(@RequestBody @Valid UserCreateRequest request) {
        return VsResponseUtil.success(HttpStatus.CREATED, userService.createUser(request));
    }

    @Operation(summary = "Update a user", description = "Update user details (Admin or self)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping(UrlConstant.User.GET_USER)
    @PreAuthorize("hasRole('ADMIN') or (hasRole('RECRUITER') or hasRole('CANDIDATE')) and #userId == authentication.principal.id")
    public ResponseEntity<?> updateUser(
            @PathVariable @Parameter(description = "User ID") String userId,
            @RequestBody @Valid UserUpdateRequest request) {
        return VsResponseUtil.success(userService.updateUser(userId, request));
    }

    @Operation(summary = "Get user by ID", description = "Get user details by ID (Admin or self)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping(UrlConstant.User.GET_USER)
    @PreAuthorize("hasRole('ADMIN') or (hasRole('RECRUITER') or hasRole('CANDIDATE')) and #userId == authentication.principal.id")
    public ResponseEntity<?> getUserById(
            @PathVariable @Parameter(description = "User ID") String userId) {
        return VsResponseUtil.success(userService.getUserById(userId));
    }

    @Operation(summary = "Get all users", description = "Get all users with pagination and filters (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping(UrlConstant.User.GET_USERS)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers(UserFilterRequest filter) {
        return VsResponseUtil.success(userService.getAllUsers(filter));
    }

    @Operation(summary = "Delete a user", description = "Soft delete a user (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @DeleteMapping(UrlConstant.User.GET_USER)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(
            @PathVariable @Parameter(description = "User ID") String userId) {
        userService.deleteUser(userId);
        return VsResponseUtil.success("User deleted successfully");
    }

    // ========== Profile APIs ==========

    @Operation(summary = "Get current user profile", description = "Get the profile of the currently authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping(UrlConstant.User.GET_CURRENT_USER)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCurrentUserProfile() {
        return VsResponseUtil.success(userService.getCurrentUserProfile());
    }

    @Operation(summary = "Update current user profile", description = "Update the profile of the currently authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @PutMapping(UrlConstant.User.UPDATE_PROFILE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateCurrentUserProfile(
            @RequestBody @Valid UserProfileUpdateRequest request) {
        return VsResponseUtil.success(userService.updateCurrentUserProfile(request));
    }

    @Operation(summary = "Change password", description = "Change the password of the currently authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid password"),
            @ApiResponse(responseCode = "401", description = "Current password is incorrect")
    })
    @PutMapping(UrlConstant.User.CHANGE_PASSWORD)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> changePassword(
            @RequestBody @Valid ChangePasswordRequest request) {
        userService.changePassword(request);
        return VsResponseUtil.success("Password changed successfully");
    }

    @Operation(summary = "Upload avatar", description = "Upload avatar URL for the currently authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Avatar uploaded successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @PutMapping(UrlConstant.User.UPLOAD_AVATAR)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> uploadAvatar(
            @RequestBody @Parameter(description = "Avatar URL") java.util.Map<String, String> request) {
        String avatarUrl = request.get("avatarUrl");
        return VsResponseUtil.success(userService.uploadAvatar(avatarUrl));
    }

    // ========== Admin APIs ==========

    @Operation(summary = "Lock user account", description = "Lock a user account (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User locked successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping(UrlConstant.User.LOCK_USER)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> lockUser(
            @PathVariable @Parameter(description = "User ID") String userId,
            @RequestBody @Valid UserStatusRequest request) {
        userService.lockUser(userId, request);
        return VsResponseUtil.success("User locked successfully");
    }

    @Operation(summary = "Unlock user account", description = "Unlock a user account (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User unlocked successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping(UrlConstant.User.UNLOCK_USER)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> unlockUser(
            @PathVariable @Parameter(description = "User ID") String userId,
            @RequestBody @Valid UserStatusRequest request) {
        userService.unlockUser(userId, request);
        return VsResponseUtil.success("User unlocked successfully");
    }

    @Operation(summary = "Change user role", description = "Change a user's role (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User role changed successfully"),
            @ApiResponse(responseCode = "404", description = "User or role not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping(UrlConstant.User.CHANGE_ROLE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> changeUserRole(
            @PathVariable @Parameter(description = "User ID") String userId,
            @RequestBody @Valid UserStatusRequest request) {
        return VsResponseUtil.success(userService.changeUserRole(userId, request));
    }

    @Operation(summary = "Get user statistics", description = "Get user statistics (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping(UrlConstant.User.STATISTICS)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserStatistics() {
        return VsResponseUtil.success(userService.getUserStatistics());
    }
}