package org.example.workhub.service.impl;


import lombok.RequiredArgsConstructor;
import org.example.workhub.constant.ErrorMessage;
import org.example.workhub.constant.RoleConstant;
import org.example.workhub.constant.SortByDataConstant;
import org.example.workhub.domain.dto.pagination.PaginationFullRequestDto;
import org.example.workhub.domain.dto.pagination.PaginationResponseDto;
import org.example.workhub.domain.dto.pagination.PaginationSortRequestDto;
import org.example.workhub.domain.dto.pagination.PagingMeta;
import org.example.workhub.domain.dto.request.UserCreateDto;
import org.example.workhub.domain.dto.request.UserUpdateDto;
import org.example.workhub.domain.dto.response.UserDto;
import org.example.workhub.domain.entity.Role;
import org.example.workhub.domain.entity.User;
import org.example.workhub.domain.mapper.UserMapper;
import org.example.workhub.domain.specification.FilterAttributeSearch;
import org.example.workhub.domain.specification.FilterProcessor;
import org.example.workhub.domain.specification.SearchOperation;
import org.example.workhub.domain.specification.SpecificationBuilder;
import org.example.workhub.exception.NotFoundException;
import org.example.workhub.repository.RoleRepository;
import org.example.workhub.repository.UserRepository;
import org.example.workhub.security.UserPrincipal;
import org.example.workhub.service.RoleService;
import org.example.workhub.service.UserService;
import org.example.workhub.util.PaginationUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final RoleRepository roleRepository;
  private final RoleService roleService;

  private final UserRepository userRepository;

  private final UserMapper userMapper;

  private final PasswordEncoder passwordEncoder;

  @Override
  public UserDto getUserById(String userId) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{userId}));
    return userMapper.toUserDto(user);
  }

  @Override
  public PaginationResponseDto<UserDto> getCustomers(PaginationFullRequestDto request) {
    //Pagination
    Pageable pageable = PaginationUtil.buildPageable(request, SortByDataConstant.USER);
    //Create Output
    return new PaginationResponseDto<>(null, null);
  }

  @Override
  public UserDto getCurrentUser(UserPrincipal principal) {
    User user = userRepository.getUser(principal);
    return userMapper.toUserDto(user);
  }

  @Override
  public void saveRefreshToken(String token) {

  }

  @Override
  public User getUserByEmail(String email) {
    return userRepository.findByEmail(email).orElseThrow(
            () -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_EMAIL, new String[]{email})
    );
  }

  @Override
  public void updateUserToken(String email, String token) {
    User currentUser = this.getUserByEmail(email);
    currentUser.setRefreshToken(token);
    userRepository.save(currentUser);
  }

  @Override
  public UserDto createUser(UserCreateDto userCreateDto) {
    if (checkEmailExists(userCreateDto.getEmail())){
      throw new NotFoundException(ErrorMessage.User.ERR_EXISTS_EMAIL, new String[]{userCreateDto.getEmail()});
    }
    User user = userMapper.toUser(userCreateDto);
    String encodePassword = passwordEncoder.encode(userCreateDto.getPassword());
    user.setPassword(encodePassword);


    Role defaultRole = roleRepository.findByNameIgnoreCase(RoleConstant.CANDIDATE)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.Role.ERR_NOT_FOUND_ROLE, new String[]{"USER"}));
    user.setRole(defaultRole);


    User savedUser = userRepository.save(user);

    return userMapper.toUserDto(savedUser);
  }

  @Override
  public UserDto updateUser(UserUpdateDto userUpdateDto) {
    User updateUser = userRepository.findById(userUpdateDto.getId()).orElseThrow(
            () -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{userUpdateDto.getId()})
    );
    if(userUpdateDto.getRole() != null){
      Role role = roleService.getRoleById(userUpdateDto.getRole().getId());
      updateUser.setRole(role);
    }

    updateUser.setUsername(userUpdateDto.getUsername());
    updateUser.setAge(Integer.parseInt(userUpdateDto.getAge()));
    updateUser.setAddress(userUpdateDto.getAddress());
    updateUser.setGender(userUpdateDto.getGender());


    updateUser = userRepository.save(updateUser);
    return userMapper.toUserDto(updateUser);
  }

  @Override
  public void deleteUser(String id) {
    User deletedUser = userRepository.findById(id).orElseThrow(
            () -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{id})
    );
    userRepository.delete(deletedUser);

  }
  @Override
  public PaginationResponseDto<UserDto> getListUser(List<String> filter, PaginationSortRequestDto paginationSortRequestDto) {
    SpecificationBuilder<User> specificationBuilder = new SpecificationBuilder<>();
    FilterProcessor process = FilterProcessor.process(specificationBuilder, filter);

    //pageale
    Pageable pageable = PaginationUtil.buildPageable(paginationSortRequestDto, SortByDataConstant.USER);

    //lay page user theo filter va them dieu kien sort
    Page<User> pageUser = userRepository.findAll(specificationBuilder.build(), pageable);
    //paging meta
    PagingMeta pagingMeta = PaginationUtil.buildPagingMeta(paginationSortRequestDto,SortByDataConstant.USER, pageUser);

    return new PaginationResponseDto<>(pagingMeta, userMapper.toUserDtos(pageUser.getContent()));
  }

  public boolean checkEmailExists(String email){
    return userRepository.existsByEmail(email);
  }

}
