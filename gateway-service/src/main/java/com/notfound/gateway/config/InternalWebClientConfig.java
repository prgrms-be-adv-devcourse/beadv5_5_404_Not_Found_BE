package com.notfound.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 서비스 간 internal API 호출용 WebClient 설정.
 *
 * 모든 요청에 X-Internal-Secret 헤더를 자동 주입한다.
 * Eureka 기반 로드밸런싱을 사용하므로 URI에 lb://service-name 형식을 사용한다.
 */
@Configuration
public class InternalWebClientConfig {

    private static final String HEADER_INTERNAL_SECRET = "X-Internal-Secret";

    @Bean
    @LoadBalanced
    public WebClient.Builder internalWebClientBuilder(@Value("${internal.secret}") String internalSecret) {
        return WebClient.builder()
                .filter((request, next) -> {
                    ClientRequest mutated = ClientRequest.from(request)
                            .header(HEADER_INTERNAL_SECRET, internalSecret)
                            .build();
                    return next.exchange(mutated);
                });
    }
}
