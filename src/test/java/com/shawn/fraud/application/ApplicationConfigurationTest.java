package com.shawn.fraud.application;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationConfigurationTest {

    @Test
    void fraudDetectCommandCacheRedisTemplate() {
        ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration();
        assertDoesNotThrow(() -> {
            applicationConfiguration.fraudDetectCommandCacheRedisTemplate(Mockito.mock(RedisConnectionFactory.class));
        });
    }
}