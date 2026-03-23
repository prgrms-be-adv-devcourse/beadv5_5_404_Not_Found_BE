package com.notfound.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * JWT 인증 GlobalFilter
 *
 * TODO: JWT 담당자가 아래 항목을 구현합니다.
 *  1. Authorization 헤더에서 Bearer 토큰 추출
 *  2. JJWT로 서명 검증 (jwt.secret-key)
 *  3. Redis 블랙리스트 조회 (blacklist:{jti})
 *  4. claims에서 sub(userId), role, email_verified 추출
 *  5. 검증 실패 시 401 반환
 *  6. 검증 성공 시 X-User-Id, X-Role 헤더 추가 후 downstream 전달
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/members/register",
            "/api/members/login",
            "/api/members/reissue",
            "/api/products"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // TODO: JWT 검증 로직 구현 전까지 모든 요청 통과
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
