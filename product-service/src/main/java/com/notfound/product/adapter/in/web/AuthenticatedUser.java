package com.notfound.product.adapter.in.web;

/**
 * Gateway가 JWT 검증 후 전달한 사용자 정보.
 * X-User-Id, X-Role, X-Email-Verified 헤더에서 추출된다.
 */
public record AuthenticatedUser(
        String userId,
        String role,
        boolean emailVerified
) {
}
