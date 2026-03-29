package com.notfound.payment.infrastructure.client;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TossProperties.class)
public class TossConfig {

    @Bean
    public TossPayClient tossPayClient(TossProperties tossProperties) {
        return new TossPayClient(tossProperties);
    }
}
