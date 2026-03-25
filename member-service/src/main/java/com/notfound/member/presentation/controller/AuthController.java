package com.notfound.member.presentation.controller;

import com.notfound.member.application.port.in.LoginUseCase;
import com.notfound.member.application.port.in.LogoutUseCase;
import com.notfound.member.application.port.in.RefreshTokenUseCase;
import com.notfound.member.application.port.in.RegisterMemberUseCase;
import com.notfound.member.application.port.in.result.AuthResult;
import com.notfound.member.domain.exception.MemberException;
import com.notfound.member.presentation.dto.ApiResponse;
import com.notfound.member.presentation.dto.AuthResponse;
import com.notfound.member.presentation.dto.LoginRequest;
import com.notfound.member.presentation.dto.LogoutRequest;
import com.notfound.member.presentation.dto.RefreshRequest;
import com.notfound.member.presentation.dto.RegisterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/auth")
public class AuthController {

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

    @Operation(summary = "회원가입", description = "신규 회원 생성 + 토큰 발급")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {

        AuthResult result = registerMemberUseCase.register(
                request.toCommand(),
                httpRequest.getHeader("User-Agent"),
                httpRequest.getRemoteAddr());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "MEMBER_REGISTER_SUCCESS",
                        "회원가입이 완료되었습니다.", AuthResponse.from(result)));
    }

    @Operation(summary = "로그인", description = "JWT 발급")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        AuthResult result = loginUseCase.login(
                request.toCommand(),
                httpRequest.getHeader("User-Agent"),
                httpRequest.getRemoteAddr());

        return ResponseEntity.ok(
                ApiResponse.success(200, "MEMBER_LOGIN_SUCCESS",
                        "로그인이 완료되었습니다.", AuthResponse.from(result)));
    }

    @Operation(summary = "토큰 재발급", description = "Access Token 재발급")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshRequest request,
            HttpServletRequest httpRequest) {

        AuthResult result = refreshTokenUseCase.refresh(
                request.refreshToken(),
                httpRequest.getHeader("User-Agent"),
                httpRequest.getRemoteAddr());

        return ResponseEntity.ok(
                ApiResponse.success(200, "MEMBER_TOKEN_REFRESH_SUCCESS",
                        "토큰이 재발급되었습니다.", AuthResponse.from(result)));
    }

    @Operation(summary = "로그아웃", description = "로그아웃 처리")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody LogoutRequest request) {

        String accessToken = authHeader != null ? extractAccessToken(authHeader) : null;
        logoutUseCase.logout(accessToken, request.refreshToken());

        return ResponseEntity.ok(
                ApiResponse.success(200, "MEMBER_LOGOUT_SUCCESS",
                        "로그아웃이 완료되었습니다.", null));
    }

    private String extractAccessToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw MemberException.invalidAccessToken();
        }
        return authHeader.substring(7);
    }
}
