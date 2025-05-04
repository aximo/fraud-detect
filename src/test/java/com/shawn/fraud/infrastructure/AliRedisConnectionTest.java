package com.shawn.fraud.infrastructure;

import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

public class AliRedisConnectionTest {

    @Test
    public void read() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://r-uf67zu1150j32w6ttupd.redis.rds.aliyuncs.com:6379")
                .setPassword("Passw0rd")
                .setConnectTimeout(10000)       // 连接超时
                .setTimeout(10000)              // 命令等待超时
                .setRetryAttempts(3)            // 重试次数
                .setRetryInterval(1500);        // 重试间隔
        RedissonClient client = Redisson.create(config);
        StringRedisTemplate redisTemplate = new StringRedisTemplate();
        redisTemplate.setConnectionFactory(new RedissonConnectionFactory(client));
        redisTemplate.afterPropertiesSet();
        redisTemplate.opsForValue().set("test", "1234");
    }
}
