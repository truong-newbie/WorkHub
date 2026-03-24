package org.example.workhub.service.impl;


import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.workhub.constant.ErrorMessage;
import org.example.workhub.constant.RoleConstant;
import org.example.workhub.domain.dto.request.LoginRequestDto;
import org.example.workhub.domain.dto.request.RegisterRequestDto;
import org.example.workhub.domain.dto.request.TokenRefreshRequestDto;
import org.example.workhub.domain.dto.response.CommonResponseDto;
import org.example.workhub.domain.dto.response.LoginResponseDto;
import org.example.workhub.domain.dto.response.RegisterResponseDto;
import org.example.workhub.domain.dto.response.TokenRefreshResponseDto;
import org.example.workhub.domain.entity.User;
import org.example.workhub.domain.mapper.UserMapper;
import org.example.workhub.exception.ConflictException;
import org.example.workhub.exception.NotFoundException;
import org.example.workhub.exception.UnauthorizedException;
import org.example.workhub.repository.RoleRepository;
import org.example.workhub.repository.UserRepository;
import org.example.workhub.security.UserPrincipal;
import org.example.workhub.security.jwt.JwtTokenProvider;
import org.example.workhub.service.AuthService;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class AuthServiceImpl implements AuthService {

  AuthenticationManager authenticationManager;
  UserRepository userRepository;
  JwtTokenProvider jwtTokenProvider;
  PasswordEncoder passwordEncoder;
  RoleRepository roleRepository;
  UserMapper userMapper;

  @Override
  public RegisterResponseDto register(RegisterRequestDto req) {
    if(userRepository.existsByEmail(req.getEmail())){
      throw new ConflictException(ErrorMessage.Auth.ERR_ALREADY_EXISTS_EMAIL);
    }

    User user= new User();
    user.setEmail(req.getEmail());
    user.setUsername(req.getUsername());
    user.setPassword(passwordEncoder.encode(req.getPassword()));
    user.setDob(req.getDob());
    user.setGender(req.getGender());
    user.setRole( roleRepository.findByName(RoleConstant.CANDIDATE)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.Role.ERR_NOT_FOUND, new String[]{RoleConstant.CANDIDATE}))
    );
    return userMapper.toRegisterDto(userRepository.save(user));
  }

  @Override
  public LoginResponseDto login(LoginRequestDto request) {
    try {
      Authentication authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(request.getEmailOrPhone(), request.getPassword()));
      SecurityContextHolder.getContext().setAuthentication(authentication);
      UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
      String accessToken = jwtTokenProvider.generateToken(userPrincipal, Boolean.FALSE);
      String refreshToken = jwtTokenProvider.generateToken(userPrincipal, Boolean.TRUE);
      return new LoginResponseDto(accessToken, refreshToken, userPrincipal.getId(), authentication.getAuthorities());
    } catch (InternalAuthenticationServiceException e) {
      throw new UnauthorizedException(ErrorMessage.Auth.ERR_INCORRECT_USERNAME);
    } catch (BadCredentialsException e) {
      throw new UnauthorizedException(ErrorMessage.Auth.ERR_INCORRECT_PASSWORD);
    }
  }

  @Override
  public TokenRefreshResponseDto refresh(TokenRefreshRequestDto request) {
    return null;
  }

  @Override
  public CommonResponseDto logout(HttpServletRequest request) {
    return null;
  }



}
