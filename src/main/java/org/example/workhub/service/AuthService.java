package org.example.workhub.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import org.example.workhub.domain.dto.request.LoginRequestDto;
import org.example.workhub.domain.dto.request.RegisterRequestDto;
import org.example.workhub.domain.dto.request.TokenRefreshRequestDto;
import org.example.workhub.domain.dto.response.CommonResponseDto;
import org.example.workhub.domain.dto.response.LoginResponseDto;
import org.example.workhub.domain.dto.response.RegisterResponseDto;
import org.example.workhub.domain.dto.response.TokenRefreshResponseDto;

import java.io.IOException;
import java.util.Map;

public interface AuthService {

//  LoginResponseDto login(LoginRequestDto request);

  TokenRefreshResponseDto refresh(TokenRefreshRequestDto request);

  CommonResponseDto logout(HttpServletRequest request);

  RegisterResponseDto register(RegisterRequestDto req);

  LoginResponseDto login(LoginRequestDto request , HttpServletRequest httpServletRequest);

  Map<String, Object> authenticateAndFetchProfile(String code, String loginType) throws IOException;

  String generateAuthUrl(String loginType);

  LoginResponseDto socialLogin(LoginRequestDto request, HttpServletRequest httpServletRequest);
}
