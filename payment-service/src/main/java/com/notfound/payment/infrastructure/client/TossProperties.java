package com.notfound.payment.infrastructure.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "toss")
public record TossProperties(
        String secretKey,
        String clientKey,
        String successUrl,
        String failUrl
) {}
