package com.shawn.fraud.application.detect;

import com.redis.testcontainers.RedisContainer;
import com.shawn.fraud.application.ApplicationConfiguration;
import com.shawn.fraud.domain.FraudError;
import com.shawn.fraud.domain.FraudService;
import com.shawn.fraud.domain.LockTemplate;
import com.shawn.fraud.domain.SimplestFraudService;
import com.shawn.fraud.domain.config.FraudDetectProperties;
import com.shawn.fraud.domain.model.Transaction;
import com.shawn.fraud.infrastructure.DistributedLockTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FraudDetectCommandHandlerTest {

    private final RedissonClient redissonClient = createRedissonClient();
    @InjectMocks
    FraudDetectCommandHandler fraudDetectCommandHandler;

    @Spy
    FraudDetectProperties fraudDetectProperties = new FraudDetectProperties();
    @Spy
    FraudService service = new SimplestFraudService();

    @Spy
    LockTemplate lockTemplate = new DistributedLockTemplate(redissonClient);

    @Spy
    RedisTemplate<String, FraudDetectCommandResult> redisTemplate = createRedisTemplate(redissonClient);

    private RedisTemplate<String, FraudDetectCommandResult> createRedisTemplate(RedissonClient redissonClient) {
        RedisTemplate<String, FraudDetectCommandResult> resultRedisTemplate = new ApplicationConfiguration()
                .fraudDetectCommandCacheRedisTemplate(new RedissonConnectionFactory(redissonClient));
        resultRedisTemplate.afterPropertiesSet();
        return resultRedisTemplate;
    }

    private RedissonClient createRedissonClient() {
        RedisContainer redisContainer = new RedisContainer(DockerImageName.parse("redis:6.2.6"));
        redisContainer.start();
        String address = redisContainer.getHost();
        Integer port = redisContainer.getMappedPort(6379);
        String redisUrl = "redis://" + address + ":" + port;

        Config config = new Config();
        config.useSingleServer().setAddress(redisUrl);
        return Redisson.create(config);
    }

    @Mock
    ApplicationEventPublisher applicationEventPublisher;

    @Test
    void execute_happen_case() {
        String requestId = UUID.randomUUID().toString();
        Transaction transaction = new Transaction();
        transaction.setCountry("china");
        transaction.setAge(20);
        transaction.setId(requestId);
        transaction.setAmount(BigDecimal.valueOf(100));
        FraudDetectCommand command = new FraudDetectCommand(requestId, transaction, false);
        FraudDetectCommandResult result = fraudDetectCommandHandler.execute(command);
        // for the first time, it should
        Mockito.verify(service, Mockito.times(1)).detect(transaction);
        assertTrue(result.isSuccess());

        // try to access again, use cache, so service not call again
        result = fraudDetectCommandHandler.execute(command);
        assertTrue(result.isSuccess());
        Mockito.verify(service, Mockito.times(1)).detect(transaction);
    }

    @Test
    void execute_error_case() {
        String requestId = UUID.randomUUID().toString();
        Transaction transaction = new Transaction();
        transaction.setCountry("china");
        transaction.setAge(10);
        transaction.setId(requestId);
        transaction.setAmount(BigDecimal.valueOf(100));
        FraudDetectCommand command = new FraudDetectCommand(requestId, transaction, false);
        FraudDetectCommandResult result = fraudDetectCommandHandler.execute(command);
        // for the first time, it should be call
        Mockito.verify(service, Mockito.times(1)).detect(transaction);
        assertFalse(result.isSuccess());
        assertEquals(FraudError.TOO_YOUNG, result.getError());

        // try to access again, use cache, so service not call again
        result = fraudDetectCommandHandler.execute(command);
        assertFalse(result.isSuccess());
        Mockito.verify(service, Mockito.times(1)).detect(transaction);
        assertEquals(FraudError.TOO_YOUNG, result.getError());
    }

}