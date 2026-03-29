package org.example.workhub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.example.workhub.base.RestApiV1;
import org.example.workhub.base.VsResponseUtil;
import org.example.workhub.constant.UrlConstant;
import org.example.workhub.domain.dto.request.LoginRequestDto;
import org.example.workhub.domain.dto.request.RegisterRequestDto;
import org.example.workhub.domain.dto.response.LoginResponseDto;
import org.example.workhub.domain.dto.response.RegisterResponseDto;
import org.example.workhub.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.Map;

@Log4j2
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
@RestApiV1
public class AuthController {
    AuthService authService;

    @Operation(summary = "API Đăng ký tài khoản")
    @PostMapping(UrlConstant.Auth.REGISTER)
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDto req){
        RegisterResponseDto response = authService.register(req);
        return VsResponseUtil.success(HttpStatus.CREATED, response);
    }


    @Operation(
            summary = "Đăng nhập bằng username & password",
            description = "Truyền vào username và password hợp lệ để nhận access token & refresh token"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Đăng nhập thành công"),
            @ApiResponse(responseCode = "401", description = "Sai thông tin đăng nhập")
    })
    @PostMapping(UrlConstant.Auth.LOGIN)
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto request, HttpServletRequest requestHttp) {
        return VsResponseUtil.success(authService.login(request, requestHttp));
    }

    @Operation(
            summary = "API Logout",
            description = "Xóa cookie chứa accessToken và refreshToken nếu có"
    )
    @PostMapping(UrlConstant.Auth.LOGOUT)
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        log.info("Nhận request logout");

        Cookie clearAccessToken = new Cookie("accessToken", "");
        clearAccessToken.setMaxAge(0);
        clearAccessToken.setPath("/");
        clearAccessToken.setHttpOnly(true);
        clearAccessToken.setSecure(true);
        response.addCookie(clearAccessToken);

        Cookie clearRefreshToken = new Cookie("refreshToken", "");
        clearRefreshToken.setMaxAge(0);
        clearRefreshToken.setPath("/");
        clearRefreshToken.setHttpOnly(true);
        clearRefreshToken.setSecure(true);
        response.addCookie(clearRefreshToken);

        log.info("Đã xóa cookie accessToken và refreshToken");

        return VsResponseUtil.success(authService.logout(request));
    }

    @GetMapping(UrlConstant.Auth.GOOGLE_AUTHORIZE)    // fix lai trong url constant
    public ResponseEntity<String> socialAuth(
            @RequestParam("login_type") String loginType,
            HttpServletRequest httpServletRequest
    ){
        loginType= loginType.trim().toLowerCase();     //bo khoang trang va chuyen chu thuong
        String url = authService.generateAuthUrl(loginType);    return ResponseEntity.ok(url);
    }

    @GetMapping(UrlConstant.Auth.GOOGLE_CALLBACK)
    public ResponseEntity<?> callback(
            @RequestParam("code") String code,
            @RequestParam(value = "state", required = false) String loginType,
            HttpServletRequest request
    ) throws IOException {

        loginType = loginType == null ? "" : loginType.trim().toLowerCase();

        Map<String, Object> userInfo = authService.authenticateAndFetchProfile(code, loginType);

        if (userInfo == null) {
            return VsResponseUtil.error(HttpStatus.BAD_REQUEST, "Failed to authenticate");
        }

        String accountId = "";
        String name = "";
        String picture = "";
        String email = "";

        if ("google".equals(loginType)) {
            accountId = (String) userInfo.getOrDefault("sub", "");
            name = (String) userInfo.getOrDefault("name", "");
            picture = (String) userInfo.getOrDefault("picture", "");
            email = (String) userInfo.getOrDefault("email", "");
        }

        LoginRequestDto loginRequestDto = LoginRequestDto.builder()
                .email(email)
                .fullname(name)
                .profileImage(picture)
                .build();

        if ("google".equals(loginType)) {
            loginRequestDto.setGoogleAccountId(accountId);
        }

        LoginResponseDto response = authService.socialLogin(loginRequestDto, request);

        return VsResponseUtil.success(response);
    }




}
