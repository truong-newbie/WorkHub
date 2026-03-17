package org.example.workhub.domain.mapper;


import org.example.workhub.domain.dto.request.UserCreateDto;
import org.example.workhub.domain.dto.response.UserDto;
import org.example.workhub.domain.entity.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

  User toUser(UserCreateDto userCreateDTO);

  @Mappings({
      @Mapping(target = "roleName", source = "user.role.name"),
  })
  UserDto toUserDto(User user);

  List<UserDto> toUserDtos(List<User> user);

}
