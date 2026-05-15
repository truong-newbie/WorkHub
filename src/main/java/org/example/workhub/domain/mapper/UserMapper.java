package org.example.workhub.domain.mapper;

import org.example.workhub.domain.dto.request.UserCreateRequest;
import org.example.workhub.domain.dto.request.UserProfileUpdateRequest;
import org.example.workhub.domain.dto.request.UserUpdateRequest;
import org.example.workhub.domain.dto.response.RegisterResponseDto;
import org.example.workhub.domain.dto.response.UserResponse;
import org.example.workhub.domain.entity.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "deleted", constant = "false")
    @Mapping(target = "refreshToken", ignore = true)
    @Mapping(target = "provider", ignore = true)
    @Mapping(target = "providerId", ignore = true)
    @Mapping(target = "resumes", ignore = true)
    @Mapping(target = "forgotPassword", ignore = true)
    User toUser(UserCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "refreshToken", ignore = true)
    @Mapping(target = "provider", ignore = true)
    @Mapping(target = "providerId", ignore = true)
    @Mapping(target = "resumes", ignore = true)
    @Mapping(target = "forgotPassword", ignore = true)
    void updateUserFromRequest(UserUpdateRequest request, @MappingTarget User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "refreshToken", ignore = true)
    @Mapping(target = "provider", ignore = true)
    @Mapping(target = "providerId", ignore = true)
    @Mapping(target = "resumes", ignore = true)
    @Mapping(target = "forgotPassword", ignore = true)
    void updateProfileFromRequest(UserProfileUpdateRequest request, @MappingTarget User user);

    @Mappings({
            @Mapping(target = "roleName", source = "user.role.name"),
            @Mapping(target = "roleId", source = "user.role.id"),
            @Mapping(target = "companyId", source = "user.company.id"),
            @Mapping(target = "companyName", source = "user.company.name"),
            @Mapping(target = "createdDate", source = "user.createdDate"),
            @Mapping(target = "lastModifiedDate", source = "user.lastModifiedDate")
    })
    UserResponse toUserResponse(User user);

    List<UserResponse> toUserResponses(List<User> users);

    RegisterResponseDto toRegisterDto(User user);
}