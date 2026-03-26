package com.notfound.member.presentation.controller;

import com.notfound.member.application.port.in.LoginUseCase;
import com.notfound.member.application.port.in.LogoutUseCase;
import com.notfound.member.application.port.in.RefreshTokenUseCase;
import com.notfound.member.application.port.in.RegisterMemberUseCase;
import com.notfound.member.application.port.in.result.AuthResult;
import com.notfound.member.presentation.dto.ApiResponse;
import com.notfound.member.presentation.dto.AuthResponse;
import com.notfound.member.presentation.dto.LoginRequest;
import com.notfound.member.presentation.dto.RegisterRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final String REFRESH_TOKEN_COOKIE = "refresh_token";
    private static final int REFRESH_TOKEN_MAX_AGE = 30 * 24 * 60 * 60; // 30일

    private final RegisterMemberUseCase registerMemberUseCase;
    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;

    public AuthController(RegisterMemberUseCase registerMemberUseCase,
                          LoginUseCase loginUseCase,
                          RefreshTokenUseCase refreshTokenUseCase,
                          LogoutUseCase logoutUseCase) {
        this.registerMemberUseCase = registerMemberUseCase;
        this.loginUseCase = loginUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.logoutUseCase = logoutUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        AuthResult result = registerMemberUseCase.register(
                request.toCommand(),
                httpRequest.getHeader("User-Agent"),
                resolveClientIp(httpRequest));

        addRefreshTokenCookie(httpResponse, result.refreshToken());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "MEMBER_REGISTER_SUCCESS",
                        "회원가입이 완료되었습니다.", AuthResponse.from(result)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        AuthResult result = loginUseCase.login(
                request.toCommand(),
                httpRequest.getHeader("User-Agent"),
                resolveClientIp(httpRequest));

        addRefreshTokenCookie(httpResponse, result.refreshToken());

        return ResponseEntity.ok(
                ApiResponse.success(200, "MEMBER_LOGIN_SUCCESS",
                        "로그인이 완료되었습니다.", AuthResponse.from(result)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @CookieValue(name = REFRESH_TOKEN_COOKIE) String refreshToken,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        AuthResult result = refreshTokenUseCase.refresh(
                refreshToken,
                httpRequest.getHeader("User-Agent"),
                resolveClientIp(httpRequest));

        addRefreshTokenCookie(httpResponse, result.refreshToken());

        return ResponseEntity.ok(
                ApiResponse.success(200, "MEMBER_TOKEN_REFRESH_SUCCESS",
                        "토큰이 재발급되었습니다.", AuthResponse.from(result)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
            HttpServletResponse httpResponse) {

        String accessToken = extractAccessToken(authHeader);
        logoutUseCase.logout(accessToken, refreshToken);

        clearRefreshTokenCookie(httpResponse);

        return ResponseEntity.ok(
                ApiResponse.success(200, "MEMBER_LOGOUT_SUCCESS",
                        "로그아웃이 완료되었습니다.", null));
    }

    private void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(REFRESH_TOKEN_MAX_AGE);
        response.addCookie(cookie);
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String extractAccessToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
