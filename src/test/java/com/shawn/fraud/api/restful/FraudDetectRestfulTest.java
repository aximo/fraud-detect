package com.shawn.fraud.api.restful;

import com.shawn.fraud.api.FraudDetectRequest;
import com.shawn.fraud.api.FraudDetectResponse;
import com.shawn.fraud.application.CommandHandler;
import com.shawn.fraud.application.detect.FraudDetectCommand;
import com.shawn.fraud.application.detect.FraudDetectCommandResult;
import com.shawn.fraud.domain.FraudError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FraudDetectRestfulTest {
    @Mock
    CommandHandler<FraudDetectCommand, FraudDetectCommandResult> commandHandler;

    @InjectMocks
    FraudDetectRestful restful;

    @BeforeEach
    public void setup() {
        clearInvocations(commandHandler);
    }

    @Test
    void detect() {

        String id = UUID.randomUUID().toString();
        doReturn(FraudDetectCommandResult.fail(FraudError.TOO_BIG_AMOUNT)).when(commandHandler).execute(any());
        FraudDetectResponse response = restful.detect(id, new FraudDetectRequest(id, 1000, 20, "china"));
        verify(commandHandler, times(1)).execute(any());
        assertEquals(FraudError.TOO_BIG_AMOUNT, response.getError());
        assertFalse(response.isSuccess());
    }

    @Test
    void detectAsync() {
        String id = UUID.randomUUID().toString();
        FraudDetectRequest request = new FraudDetectRequest(id, 1000, 20, "china");
        restful.detectAsync(id, request);
        verify(commandHandler, times(1)).execute(any());
    }
}