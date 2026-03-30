package com.notfound.product.adapter.in.web.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * /internal/** 경로에 대한 서비스 간 통신 인증 필터.
 *
 * X-Internal-Secret 헤더가 없거나 값이 일치하지 않으면 403을 반환한다.
 * /internal/** 이외의 경로는 필터를 통과시킨다.
 */
@Component
public class InternalSecretFilter extends OncePerRequestFilter {

    private static final String INTERNAL_PATH_PREFIX = "/internal/";
    private static final String HEADER_INTERNAL_SECRET = "X-Internal-Secret";

    private final String internalSecret;

    public InternalSecretFilter(@Value("${internal.secret}") String internalSecret) {
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
        if (secret == null || !secret.equals(internalSecret)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    "{\"status\":403,\"code\":\"ACCESS_DENIED\",\"message\":\"내부 API 접근 권한이 없습니다.\",\"data\":null}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
