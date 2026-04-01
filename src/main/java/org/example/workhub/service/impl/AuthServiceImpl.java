package org.example.workhub.service.impl;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import io.jsonwebtoken.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.workhub.constant.ErrorMessage;
import org.example.workhub.constant.RoleConstant;
import org.example.workhub.domain.dto.common.FacebookProperties;
import org.example.workhub.domain.dto.common.FacebookProviderProperties;
import org.example.workhub.domain.dto.common.GoogleProperties;
import org.example.workhub.domain.dto.common.GoogleProviderProperties;
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
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
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
  GoogleProperties googleProperties;
  GoogleProviderProperties googleProviderProperties;
  FacebookProperties facebookProperties;
  FacebookProviderProperties facebookProviderProperties;


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
            .orElseThrow(() -> new NotFoundException(ErrorMessage.Role.ERR_NOT_FOUND_ROLE, new String[]{RoleConstant.CANDIDATE}))
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


  public Map<String, Object> authenticateAndFetchProfile(String code, String loginType) throws IOException, java.io.IOException {
    RestTemplate restTemplate = new RestTemplate();
    restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());

    String accessToken;

    if (loginType == null) return null;

    switch (loginType.toLowerCase()) {
      case "google":
        accessToken = new GoogleAuthorizationCodeTokenRequest(
                new NetHttpTransport(), new GsonFactory(),
                googleProperties.getClientId(),
                googleProperties.getClientSecret(),
                code,
                googleProperties.getRedirectUri()
        ).execute().getAccessToken();

        // Configure RestTemplate to include the access token in the Authorization header
        restTemplate.getInterceptors().add((req, body, executionContext) -> {
          req.getHeaders().set("Authorization", "Bearer " + accessToken);
          return executionContext.execute(req, body);
        });

        // Make a GET request to fetch user information
        return new ObjectMapper().readValue(
                restTemplate.getForEntity(googleProviderProperties.getUserInfoUri(), String.class).getBody(),
                new TypeReference<>() {}
        );
        //break;

      case "facebook":
        // Facebook token request setup
        String urlGetAccessToken = UriComponentsBuilder
                .fromUriString(facebookProviderProperties.getTokenUri())
                .queryParam("client_id", facebookProperties.getClientId())
                .queryParam("redirect_uri", facebookProperties.getRedirectUri())
                .queryParam("client_secret", facebookProperties.getClientSecret())
                .queryParam("code", code)
                .toUriString();

        // Use RestTemplate to fetch the Facebook access token
        ResponseEntity<String> response = restTemplate.getForEntity(urlGetAccessToken, String.class);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(response.getBody());
        accessToken = node.get("access_token").asText();

        // Set the URL for the Facebook API to fetch user info
        // Lấy thông tin người dùng
        String userInfoUri = facebookProviderProperties.getUserInfoUri() + "&access_token=" + accessToken;
        return mapper.readValue(
                restTemplate.getForEntity(userInfoUri, String.class).getBody(),
                new TypeReference<>() {}
        );
        //break;

      default:
        System.out.println("Unsupported login type: " + loginType);
        return null;
    }

  }

  @Override
  public String generateAuthUrl(String loginType) {

    if ("google".equalsIgnoreCase(loginType)) {
      return "https://accounts.google.com/o/oauth2/v2/auth"
              + "?client_id=" + googleProperties.getClientId()
              + "&redirect_uri=" + googleProperties.getRedirectUri()
              + "&response_type=code"
              + "&scope=openid%20email%20profile"
              + "&state=google";
    }

    if ("facebook".equalsIgnoreCase(loginType)) {
      return "https://www.facebook.com/v18.0/dialog/oauth"
              + "?client_id=" + facebookProperties.getClientId()
              + "&redirect_uri=" + facebookProperties.getRedirectUri()
              + "&response_type=code"
              + "&scope=email,public_profile"
              + "&state=facebook";
    }

    return null;
  }

  @Override
  public LoginResponseDto socialLogin(LoginRequestDto request, HttpServletRequest httpServletRequest) {

    User user = userRepository.findByEmail(request.getEmail())
            .orElseGet(() -> {
              User newUser = new User();
              newUser.setEmail(request.getEmail());
              newUser.setUsername(request.getFullname());
              newUser.setPassword("SOCIAL_LOGIN"); // không dùng password
              newUser.setRole(roleRepository.findByName(RoleConstant.CANDIDATE)
                      .orElseThrow(() -> new NotFoundException(
                              ErrorMessage.Role.ERR_NOT_FOUND_ROLE,
                              new String[]{RoleConstant.CANDIDATE}
                      )));

              if (request.getGoogleAccountId() != null) {
                newUser.setProvider("GOOGLE");
                newUser.setProviderId(request.getGoogleAccountId());
              } else if (request.getFacebookAccountId() != null) {
                newUser.setProvider("FACEBOOK");
                newUser.setProviderId(request.getFacebookAccountId());
              }
              return userRepository.save(newUser);
            });

    if (user.getProvider() != null) {
      if (request.getGoogleAccountId() != null && !"GOOGLE".equals(user.getProvider())) {
        throw new ConflictException("Email đã đăng ký bằng phương thức khác");
      }

      if (request.getFacebookAccountId() != null && !"FACEBOOK".equals(user.getProvider())) {
        throw new ConflictException("Email đã đăng ký bằng phương thức khác");
      }
    }
    // tạo UserPrincipal
    UserPrincipal userPrincipal = UserPrincipal.create(user);

    //  Generate token
    String accessToken = jwtTokenProvider.generateToken(userPrincipal, false);
    String refreshToken = jwtTokenProvider.generateToken(userPrincipal, true);

    //  Lưu session
    String ipAddress = TokenBlacklistUtil.getClientIP(httpServletRequest);

    UserSession userSession = userSessionRepository.findByIpAddressAndEmail(ipAddress, user.getEmail());

    if (userSession == null) {
      userSession = new UserSession();
      userSession.setIpAddress(ipAddress);
      userSession.setEmail(user.getEmail());
      userSession.setUser(user);
    }

    userSession.setToken(accessToken);
    userSession.setRefreshToken(refreshToken);
    userSession.setIsActive(true);

    userSessionRepository.save(userSession);

    return new LoginResponseDto(accessToken, refreshToken, user.getId(), userPrincipal.getAuthorities());
  }

}
