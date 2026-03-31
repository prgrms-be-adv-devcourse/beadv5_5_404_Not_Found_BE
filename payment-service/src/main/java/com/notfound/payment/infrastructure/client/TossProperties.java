package com.notfound.payment.infrastructure.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "toss.payments")
public record TossProperties(
        String secretKey,
        String clientKey,
        String confirmUrl,
        String successUrl,
        String failUrl
) {}
