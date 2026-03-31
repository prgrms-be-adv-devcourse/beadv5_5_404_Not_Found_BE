package com.notfound.order.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * /internal/** 경로에 대한 서비스 간 통신 인증 필터.
 *
 * 보안 규약:
 * - X-Internal-Secret 헤더 필수. 불일치/누락 시 403 반환.
 * - 시크릿은 환경변수로 주입. 기본값 사용 금지 (기동 시 검증).
 * - 로그에 시크릿/민감 헤더값을 마스킹하여 출력.
 * - 잘못된 내부 호출은 감사 로그로 기록 (요청 경로 + 호출자 IP).
 */
@Component
public class InternalSecretFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(InternalSecretFilter.class);

    private static final String INTERNAL_PATH_PREFIX = "/internal/";
    private static final String HEADER_INTERNAL_SECRET = "X-Internal-Secret";
    private static final String FORBIDDEN_DEFAULT = "your_internal_service_secret_key_here";

    private final String internalSecret;

    public InternalSecretFilter(@Value("${internal.secret-key}") String internalSecret) {
        // 기본값 또는 미설정 시 기동 실패
        if (internalSecret == null || internalSecret.isBlank() || FORBIDDEN_DEFAULT.equals(internalSecret)) {
            throw new IllegalStateException(
                    "[SECURITY] internal.secret-key가 설정되지 않았거나 기본값입니다. " +
                    "환경변수 INTERNAL_SECRET_KEY를 설정하세요.");
        }
        this.internalSecret = internalSecret;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        if (!path.startsWith(INTERNAL_PATH_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String secret = request.getHeader(HEADER_INTERNAL_SECRET);
        if (secret == null || !java.security.MessageDigest.isEqual(
                secret.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                internalSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8))) {
            // 감사 로그: 잘못된 내부 호출 기록 (시크릿 값은 마스킹)
            String maskedSecret = secret == null ? "null" : "***" + secret.substring(Math.max(0, secret.length() - 4));
            String callerIp = resolveCallerIp(request);
            log.warn("[AUDIT] 내부 API 인증 실패: path={}, callerIp={}, secret={}", path, callerIp, maskedSecret);

            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    "{\"status\":403,\"code\":\"ACCESS_DENIED\",\"message\":\"내부 API 접근 권한이 없습니다.\",\"data\":null}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String resolveCallerIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
