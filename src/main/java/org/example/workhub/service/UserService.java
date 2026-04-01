package org.example.workhub.service;


import org.example.workhub.domain.dto.pagination.PaginationFullRequestDto;
import org.example.workhub.domain.dto.pagination.PaginationResponseDto;
import org.example.workhub.domain.dto.pagination.PaginationSortRequestDto;
import org.example.workhub.domain.dto.request.UserCreateDto;
import org.example.workhub.domain.dto.request.UserUpdateDto;
import org.example.workhub.domain.dto.response.UserDto;
import org.example.workhub.domain.entity.User;
import org.example.workhub.security.UserPrincipal;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {

  UserDto getUserById(String userId);

  PaginationResponseDto<UserDto> getCustomers(PaginationFullRequestDto request);

  UserDto getCurrentUser(UserPrincipal principal);

  void saveRefreshToken(String token);

  User getUserByEmail(String email);

  void updateUserToken(String email, String token);

  UserDto createUser(UserCreateDto userCreateDto);

  UserDto updateUser(UserUpdateDto userUpdateDto);

  void deleteUser(String id);

  PaginationResponseDto<UserDto>  getListUser(List<String> filter, PaginationSortRequestDto paginationSortRequestDto);


}
