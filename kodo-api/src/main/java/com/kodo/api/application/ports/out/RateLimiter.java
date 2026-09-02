package com.kodo.api.application.ports.out;

import com.kodo.api.application.dto.RateLimitResult;

public interface RateLimiter {

    RateLimitResult tryConsume(String key, long tokens);
}
