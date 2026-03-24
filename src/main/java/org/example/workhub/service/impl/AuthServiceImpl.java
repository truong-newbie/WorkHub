package org.example.workhub.service.impl;


import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
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
import org.example.workhub.domain.entity.UserSession;
import org.example.workhub.domain.mapper.UserMapper;
import org.example.workhub.exception.ConflictException;
import org.example.workhub.exception.NotFoundException;
import org.example.workhub.exception.UnauthorizedException;
import org.example.workhub.repository.RoleRepository;
import org.example.workhub.repository.TokenBlacklistRepository;
import org.example.workhub.repository.UserRepository;
import org.example.workhub.repository.UserSessionRepository;
import org.example.workhub.security.UserPrincipal;
import org.example.workhub.security.jwt.JwtTokenProvider;
import org.example.workhub.service.AuthService;
import org.example.workhub.util.TokenBlacklistUtil;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
@Slf4j
public class AuthServiceImpl implements AuthService {

  AuthenticationManager authenticationManager;
  UserRepository userRepository;
  JwtTokenProvider jwtTokenProvider;
  PasswordEncoder passwordEncoder;
  RoleRepository roleRepository;
  UserMapper userMapper;
  UserSessionRepository userSessionRepository;
  TokenBlacklistRepository tokenBlacklistRepository;


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
  public LoginResponseDto login(LoginRequestDto request, HttpServletRequest httpServletRequest) {
    try {
      List<UserSession> userSessions= userSessionRepository.findAllByEmail(request.getEmail());

      if(!userSessions.isEmpty()){
        for(UserSession userSession: userSessions){
          if(userSession.getIsActive())  throw new ConflictException((ErrorMessage.Auth.ERR_ALREADY_LOGGED_IN));
        }
      }

      Authentication authentication = authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
      SecurityContextHolder.getContext().setAuthentication(authentication);

      UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
      String accessToken = jwtTokenProvider.generateToken(userPrincipal, Boolean.FALSE);
      String refreshToken = jwtTokenProvider.generateToken(userPrincipal, Boolean.TRUE);

      User user = userRepository.findById(userPrincipal.getId())
              .orElseThrow(()-> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID , new String[]{userPrincipal.getId()}));

      String ipAddress = TokenBlacklistUtil.getClientIP(httpServletRequest);


      UserSession userSession = userSessionRepository.findByIpAddressAndEmail(ipAddress, userPrincipal.getUsername());

      if (userSession == null) {
        userSession = new UserSession();
        userSession.setIpAddress(ipAddress);
        userSession.setToken(accessToken);
        userSession.setRefreshToken(refreshToken);
        userSession.setEmail(userPrincipal.getUsername());
        userSession.setUser(user);
        userSession.setIsActive(true);
      }
      else {
        userSession.setToken(accessToken);
        userSession.setRefreshToken(refreshToken);
        userSession.setIsActive(true);
      }
      userSessionRepository.save(userSession);
      return new LoginResponseDto(accessToken, refreshToken, userPrincipal.getId(), authentication.getAuthorities());
    } catch (InternalAuthenticationServiceException e) {
      throw new UnauthorizedException(ErrorMessage.Auth.ERR_INCORRECT_EMAIL);
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
    log.info("Processing logout request");

    String bearerToken = request.getHeader("Authorization");
    if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
      throw new UnauthorizedException(ErrorMessage.UNAUTHORIZED);
    }

    String token = bearerToken.substring(7);
    log.info("Logout token: {}", token);

    UserSession userSession = userSessionRepository.findByToken(token);
    if (userSession == null) {
      throw new UnauthorizedException(ErrorMessage.UNAUTHORIZED);
    }

    String refreshToken = userSession.getRefreshToken();

    userSession.setIsActive(false);

    userSessionRepository.save(userSession);

    TokenBlacklistUtil.addTokenToBlacklist(token, "Logout access token", tokenBlacklistRepository);

    if (refreshToken != null && !refreshToken.isBlank()) {
      TokenBlacklistUtil.addTokenToBlacklist(
              refreshToken,
              "Logout refresh token",
              tokenBlacklistRepository
      );
    }

    SecurityContextHolder.clearContext();

    return new CommonResponseDto(true, "Logged out successfully");
  }



}
