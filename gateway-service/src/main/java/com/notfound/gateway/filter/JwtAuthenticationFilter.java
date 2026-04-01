package com.notfound.gateway.filter;

import com.notfound.gateway.client.MemberInternalClient;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

/**
 * Gateway JWT 인증 GlobalFilter
 *
 * 1. 공개 API는 인증 없이 통과
 * 2. Authorization 헤더에서 Bearer 토큰 추출
 * 3. JJWT로 서명 검증 + 만료 확인
 * 4. Member Service internal API로 블랙리스트 조회
 * 5. claims에서 sub(userId), role, email_verified 추출
 * 6. 검증 실패 시 401 반환
 * 7. 검증 성공 시 X-User-Id, X-Role, X-Email-Verified 헤더 추가 후 downstream 전달
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_ROLE = "X-Role";
    private static final String HEADER_EMAIL_VERIFIED = "X-Email-Verified";

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/member/auth/register",
            "/api/member/auth/login",
            "/api/member/auth/refresh",
            "/member/swagger-ui",
            "/member/v3/api-docs",
            "/order/swagger-ui",
            "/order/v3/api-docs",
            "/payment/swagger-ui",
            "/payment/v3/api-docs",
            "/webjars"
    );

    private final SecretKey secretKey;
    private final MemberInternalClient memberInternalClient;

    public JwtAuthenticationFilter(@Value("${jwt.secret}") String secret,
                                   MemberInternalClient memberInternalClient) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.memberInternalClient = memberInternalClient;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        HttpMethod method = request.getMethod();

        // 공개 API 바이패스
        if (isPublicPath(path, method)) {
            return chain.filter(stripUserHeaders(exchange));
        }

        // Authorization 헤더 확인
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return unauthorized(exchange);
        }

        // JWT 검증
        String token = authHeader.substring(BEARER_PREFIX.length());
        Claims claims;
        try {
            claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            return unauthorized(exchange);
        }

        String jti = claims.getId();
        String userId = claims.getSubject();
        String role = claims.get("role", String.class);
        Boolean emailVerified = claims.get("email_verified", Boolean.class);

        if (jti == null || userId == null || role == null || emailVerified == null) {
            return unauthorized(exchange);
        }

        // 블랙리스트 조회 후 헤더 주입 (조회 실패 시 fail-closed → 401)
        return memberInternalClient.isBlacklisted(jti)
                .timeout(Duration.ofMillis(300))
                .onErrorResume(e -> {
                    log.error("Blacklist lookup failed for jti={}", jti, e);
                    return Mono.just(Boolean.TRUE);
                })
                .flatMap(blacklisted -> {
                    if (Boolean.TRUE.equals(blacklisted)) {
                        return unauthorized(exchange);
                    }

                    ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                            .headers(headers -> {
                                headers.remove(HEADER_USER_ID);
                                headers.remove(HEADER_ROLE);
                                headers.remove(HEADER_EMAIL_VERIFIED);
                            })
                            .header(HEADER_USER_ID, userId)
                            .header(HEADER_ROLE, role)
                            .header(HEADER_EMAIL_VERIFIED, emailVerified.toString())
                            .build();

                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                })
                .onErrorResume(e -> unauthorized(exchange));
    }

    private boolean isPublicPath(String path, HttpMethod method) {
        if (HttpMethod.GET.equals(method) && path.startsWith("/api/product")) {
            return true;
        }
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private ServerWebExchange stripUserHeaders(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest().mutate()
                .headers(headers -> {
                    headers.remove(HEADER_USER_ID);
                    headers.remove(HEADER_ROLE);
                    headers.remove(HEADER_EMAIL_VERIFIED);
                })
                .build();
        return exchange.mutate().request(request).build();
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
