package com.shawn.fraud.application;

import com.shawn.fraud.application.detect.FraudDetectCommandResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public RedisTemplate<String, FraudDetectCommandResult> fraudDetectCommandCacheRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, FraudDetectCommandResult> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(FraudDetectCommandResult.class));
        return redisTemplate;
    }
}
