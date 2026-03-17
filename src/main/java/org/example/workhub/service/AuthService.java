package org.example.workhub.service;


import jakarta.servlet.http.HttpServletRequest;
import org.example.workhub.domain.dto.request.LoginRequestDto;
import org.example.workhub.domain.dto.request.TokenRefreshRequestDto;
import org.example.workhub.domain.dto.response.CommonResponseDto;
import org.example.workhub.domain.dto.response.LoginResponseDto;
import org.example.workhub.domain.dto.response.TokenRefreshResponseDto;

public interface AuthService {

  LoginResponseDto login(LoginRequestDto request);

  TokenRefreshResponseDto refresh(TokenRefreshRequestDto request);

  CommonResponseDto logout(HttpServletRequest request);

}
