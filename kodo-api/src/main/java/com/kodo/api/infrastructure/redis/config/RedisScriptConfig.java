package com.kodo.api.infrastructure.redis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;

@Configuration
public class RedisScriptConfig {

    @Bean
    public RedisScript<List> tokenBucketScript() {
        return RedisScript.of(
                new ClassPathResource("redis/token-bucket.lua"),
                List.class
        );
    }
}