package com.notfound.member.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * Gateway가 전달한 X-User-Id, X-Role 헤더를 기반으로 경로별 접근 권한을 검증한다.
 *
 * - /auth/register, /auth/login, /auth/refresh → 공개 (인증 불필요)
 * - /auth/logout                               → 로그인 필요
 * - /member/**                                 → 로그인 필요 (USER, SELLER, ADMIN)
 * - /admin/**                                  → ADMIN만
 * - /internal/**                               → InternalSecretFilter에서 처리 (이 필터 통과)
 */
@Component
public class RoleAuthorizationFilter extends OncePerRequestFilter {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_ROLE = "X-Role";

    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/auth/register",
            "/auth/login",
            "/auth/refresh"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        // 공개 API → 통과
        if (PUBLIC_PATHS.contains(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // GET /member/seller/{memberId} → 공개 (판매자 정보 조회)
        if ("GET".equalsIgnoreCase(request.getMethod()) && path.startsWith("/member/seller/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // /internal/** → InternalSecretFilter에서 처리
        if (path.startsWith("/internal/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Swagger/OpenAPI → 통과
        if (path.startsWith("/swagger") || path.startsWith("/v3/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 이하 경로는 로그인 필요
        String userId = request.getHeader(HEADER_USER_ID);
        if (userId == null || userId.isBlank()) {
            sendError(response, HttpStatus.UNAUTHORIZED, "MEMBER_UNAUTHORIZED", "로그인이 필요합니다.");
            return;
        }

        // /admin/** 또는 /member/admin/** → ADMIN만 접근 가능
        if (path.startsWith("/admin/") || path.startsWith("/member/admin/")) {
            String role = request.getHeader(HEADER_ROLE);
            if (!"ADMIN".equals(role)) {
                sendError(response, HttpStatus.FORBIDDEN, "MEMBER_FORBIDDEN", "관리자만 접근할 수 있습니다.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void sendError(HttpServletResponse response, HttpStatus status, String code, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                "{\"status\":%d,\"code\":\"%s\",\"message\":\"%s\",\"data\":null}"
                        .formatted(status.value(), code, message));
    }
}
