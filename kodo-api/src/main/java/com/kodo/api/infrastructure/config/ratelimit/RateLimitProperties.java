package com.kodo.api.infrastructure.config.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kodo.rate-limit")
public record RateLimitProperties(
        boolean enabled,
        long capacity,
        double refillRate
) {
}
