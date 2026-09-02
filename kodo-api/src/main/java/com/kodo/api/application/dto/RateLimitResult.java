package com.kodo.api.application.dto;

public record RateLimitResult(
        boolean allowed,
        long remainingTokens,
        long retryAfterSeconds
) {
}
