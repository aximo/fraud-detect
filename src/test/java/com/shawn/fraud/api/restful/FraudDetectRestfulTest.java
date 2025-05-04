package com.shawn.fraud.api.restful;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shawn.fraud.api.FraudDetectRequest;
import com.shawn.fraud.api.FraudDetectResponse;
import com.shawn.fraud.application.detect.FraudDetectCommandHandler;
import com.shawn.fraud.application.detect.FraudDetectCommandResult;
import com.shawn.fraud.domain.FraudError;
import com.shawn.fraud.domain.FraudService;
import com.shawn.fraud.domain.LockTemplate;
import com.shawn.fraud.domain.SimpleMessageTemplate;
import com.shawn.fraud.domain.config.FraudDetectProperties;
import com.shawn.fraud.domain.model.Transaction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class FraudDetectRestfulTest {
    @Mock
    FraudService fraudService;

    @Mock
    LockTemplate lockTemplate;

    @Mock
    ApplicationEventPublisher applicationEventPublisher;

    @Mock
    SimpleMessageTemplate<Transaction> simpleMessageTemplate;

    @Spy
    RedisTemplate<String, FraudDetectCommandResult> redisTemplate = new RedisTemplate<>();

    @Spy
    FraudDetectCommandHandler fraudDetectCommandHandler = new FraudDetectCommandHandler(
            new FraudDetectProperties(),
            fraudService,
            lockTemplate,
            redisTemplate,
            applicationEventPublisher
    );

    @Spy
    ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    FraudDetectRestful restful;

    @Test
    void detect() {
        doReturn(FraudDetectCommandResult.fail(FraudError.TOO_BIG_AMOUNT))
                .when(fraudDetectCommandHandler).execute(any());

        String id = UUID.randomUUID().toString();
        FraudDetectResponse response = restful.detect(id, new FraudDetectRequest(id, 1000, 20, "china"));
        Assertions.assertEquals(FraudError.TOO_BIG_AMOUNT, response.getError());
    }

    @Test
    void detectAsync() {

        String id = UUID.randomUUID().toString();
        FraudDetectRequest request = new FraudDetectRequest(id, 1000, 20, "china");
        Assertions.assertDoesNotThrow(() -> restful.detectAsync(id, request));
    }
}