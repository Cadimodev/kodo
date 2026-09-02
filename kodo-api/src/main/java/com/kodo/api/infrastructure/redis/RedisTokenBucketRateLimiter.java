package com.kodo.api.infrastructure.redis;

import com.kodo.api.application.dto.RateLimitResult;
import com.kodo.api.application.ports.out.RateLimiter;
import com.kodo.api.infrastructure.config.ratelimit.RateLimitProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RedisTokenBucketRateLimiter implements RateLimiter {

    private static final Logger log = LoggerFactory.getLogger(RedisTokenBucketRateLimiter.class);
    private static final String KEY_PREFIX = "kodo:rate-limit:";

    private final StringRedisTemplate redisTemplate;
    private final RedisScript<List> tokenBucketScript;
    private final RateLimitProperties properties;

    public RedisTokenBucketRateLimiter(StringRedisTemplate redisTemplate, RedisScript<List> tokenBucketScript, RateLimitProperties properties) {
        this.redisTemplate = redisTemplate;
        this.tokenBucketScript = tokenBucketScript;
        this.properties = properties;
    }

    @Override
    public RateLimitResult tryConsume(String key, long tokens) {

        if (!properties.enabled()) {
            log.warn("Rate limiter not enabled. Failing open");
            return new RateLimitResult(true, properties.capacity(), 0);
        }

        String redisKey = KEY_PREFIX + key;
        long ttlMillis = calculateTtlMillis();

        try {
            List<?> result = redisTemplate.execute(
                    tokenBucketScript,
                    List.of(redisKey),
                    String.valueOf(properties.capacity()),
                    String.valueOf(properties.refillRate()),
                    String.valueOf(tokens),
                    String.valueOf(ttlMillis)
            );

            if (result == null || result.size() != 3) {
                throw new IllegalStateException("Unexpected result returned by token bucket script");
            }

            boolean allowed = ((Number) result.getFirst()).longValue() == 1;
            long remainingTokens = ((Number) result.get(1)).longValue();
            long retryAfterSeconds = ((Number) result.get(2)).longValue();

            return new RateLimitResult(
                    allowed,
                    remainingTokens,
                    retryAfterSeconds
            );

        } catch (Exception e) {
            log.warn("Rate limiter unavailable for key '{}'. Failing open", key, e);

            return new RateLimitResult(true, properties.capacity(), 0);
        }
    }

    private long calculateTtlMillis() {
        double secondsToFull = properties.capacity() / properties.refillRate();

        return (long) Math.ceil(secondsToFull * 2 * 1000);
    }
}
