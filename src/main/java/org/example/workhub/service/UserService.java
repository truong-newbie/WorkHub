package org.example.workhub.service;


import org.example.workhub.domain.dto.pagination.PaginationFullRequestDto;
import org.example.workhub.domain.dto.pagination.PaginationResponseDto;
import org.example.workhub.domain.dto.response.UserDto;
import org.example.workhub.security.UserPrincipal;

public interface UserService {

  UserDto getUserById(String userId);

  PaginationResponseDto<UserDto> getCustomers(PaginationFullRequestDto request);

  UserDto getCurrentUser(UserPrincipal principal);

}
