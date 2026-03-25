package com.notfound.member.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Gateway가 전달한 X-User-Id, X-Role 헤더로 AuthenticatedUser를 주입한다.
 * X-Gateway-Auth 헤더가 internal secret과 일치하지 않으면 인증을 거부한다.
 */
@Component
public class HeaderAuthArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_ROLE = "X-Role";
    private static final String HEADER_GATEWAY_AUTH = "X-Gateway-Auth";

    private final String internalSecret;

    public HeaderAuthArgumentResolver(@Value("${internal.secret-key}") String internalSecret) {
        this.internalSecret = internalSecret;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthUser.class)
                && parameter.getParameterType().equals(AuthenticatedUser.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        String gatewayAuth = webRequest.getHeader(HEADER_GATEWAY_AUTH);
        if (gatewayAuth == null || !gatewayAuth.equals(internalSecret)) {
            return null;
        }

        String userId = webRequest.getHeader(HEADER_USER_ID);
        String role = webRequest.getHeader(HEADER_ROLE);

        if (userId == null) {
            return null;
        }

        return new AuthenticatedUser(userId, role);
    }
}
