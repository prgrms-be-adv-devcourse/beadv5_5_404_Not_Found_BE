package com.notfound.payment.infrastructure.client;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InternalFeignConfig {

    private static final String HEADER_INTERNAL_SECRET = "X-Internal-Secret";

    @Bean
    public RequestInterceptor internalSecretInterceptor(@Value("${internal.secret-key}") String internalSecret) {
        return template -> template.header(HEADER_INTERNAL_SECRET, internalSecret);
    }
}
