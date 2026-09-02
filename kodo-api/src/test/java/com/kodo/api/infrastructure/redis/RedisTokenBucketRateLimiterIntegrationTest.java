package com.kodo.api.infrastructure.redis;

import com.kodo.api.application.ports.out.RateLimiter;
import com.kodo.api.infrastructure.config.ratelimit.RateLimitProperties;
import com.kodo.api.infrastructure.redis.config.RedisScriptConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(
        classes = RedisTokenBucketRateLimiterIntegrationTest.TestConfig.class,
        properties = {
                "kodo.rate-limit.enabled=true",
                "kodo.rate-limit.capacity=3",
                "kodo.rate-limit.refill-rate=1"
        }
)
class RedisTokenBucketRateLimiterIntegrationTest {

    @Container
    static final GenericContainer<?> redis =
            new GenericContainer<>(DockerImageName.parse("redis:8-alpine"))
                    .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add(
                "spring.data.redis.port",
                () -> redis.getMappedPort(6379)
        );
    }

    @Autowired
    private RateLimiter rateLimiter;

    @Test
    void shouldRejectRequestWhenBucketIsEmpty() {
        String key = "test-game-" + UUID.randomUUID();

        assertThat(rateLimiter.tryConsume(key, 1).allowed()).isTrue();
        assertThat(rateLimiter.tryConsume(key, 1).allowed()).isTrue();
        assertThat(rateLimiter.tryConsume(key, 1).allowed()).isTrue();

        var rejected = rateLimiter.tryConsume(key, 1);

        assertThat(rejected.allowed()).isFalse();
        assertThat(rejected.remainingTokens()).isZero();
        assertThat(rejected.retryAfterSeconds()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void shouldRefillTokensOverTime() throws InterruptedException {
        String key = "test-game-" + UUID.randomUUID();

        // Empty the bucket in a single operation.
        var consumed = rateLimiter.tryConsume(key, 3);

        assertThat(consumed.allowed()).isTrue();
        assertThat(consumed.remainingTokens()).isZero();

        // No token should be available immediately.
        var rejected = rateLimiter.tryConsume(key, 1);

        assertThat(rejected.allowed()).isFalse();

        // refillRate = 1 token/second
        Thread.sleep(1_200);

        var afterRefill = rateLimiter.tryConsume(key, 1);

        assertThat(afterRefill.allowed()).isTrue();
    }

    @Configuration
    @EnableAutoConfiguration(exclude = {
            DataSourceAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class,
            DataJpaRepositoriesAutoConfiguration.class
    })
    @EnableConfigurationProperties(RateLimitProperties.class)
    @Import({
            RedisScriptConfig.class,
            RedisTokenBucketRateLimiter.class
    })
    static class TestConfig {
    }
}