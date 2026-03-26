package com.notfound.gateway.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Gateway → Member Service internal API 호출 클라이언트.
 *
 * X-Internal-Secret 헤더는 InternalWebClientConfig에서 자동 주입된다.
 */
@Component
public class MemberInternalClient {

    private static final String MEMBER_SERVICE_BASE = "lb://member-service";

    private final WebClient webClient;

    public MemberInternalClient(WebClient.Builder internalWebClientBuilder) {
        this.webClient = internalWebClientBuilder.baseUrl(MEMBER_SERVICE_BASE).build();
    }

    /**
     * Access Token의 jti가 블랙리스트에 등록되어 있는지 조회한다.
     *
     * @param jti Access Token 고유 식별자
     * @return 블랙리스트 등록 여부
     */
    public Mono<Boolean> isBlacklisted(String jti) {
        return webClient.get()
                .uri("/internal/token-blacklist/{jti}", jti)
                .retrieve()
                .bodyToMono(Boolean.class)
                .onErrorReturn(true);
    }
}
