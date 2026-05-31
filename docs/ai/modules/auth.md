# Auth Module

## Purpose

Handles registration, login, logout, refresh token, OAuth2 callback support, forgot password, JWT generation, and current user authentication.

## Main Files

- `controller/AuthController.java`
- `controller/ForgotPasswordController.java`
- `service/AuthService.java`
- `service/impl/AuthServiceImpl.java`
- `security/jwt/*`
- `security/WebSecurityConfig.java`
- `domain.dto.request/LoginRequestDto.java`
- `domain.dto.response/LoginResponseDto.java`

## Key APIs

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/logout`
- `GET /api/v1/auth/oauth2/authorize?login_type=google|facebook`
- `GET /api/v1/auth/oauth2/callback?code=...&state=google|facebook`
- Forgot password endpoints under `/api/v1/forgot-password/**`

## OAuth Docs

Detailed social login flow and test examples:

```text
docs/oauth-social-login-api.md
```

Detailed explanation of register/login/logout/OAuth internals:

```text
docs/auth-flow-explained.md
```

OAuth callback currently redirects to the frontend callback URL configured by:

```properties
auth.oauth2.frontend-callback-url
```

## Security

Auth endpoints are public. JWT-protected endpoints depend on `JwtAuthenticationFilter`.

## AI Notes

When changing auth, also inspect:

- `WebSecurityConfig`
- `JwtTokenProvider`
- `JwtAuthenticationFilter`
- `UserPrincipal`
- `CustomUserDetailsServiceImpl`
