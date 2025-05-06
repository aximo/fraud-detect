package com.shawn.fraud.application.detect;

import com.shawn.fraud.domain.SimpleMessageTemplate;
import com.shawn.fraud.domain.config.FraudDetectProperties;
import com.shawn.fraud.domain.event.FraudDetectRequestEvent;
import com.shawn.fraud.domain.model.Transaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FraudDetectRequestEventListenerTest {

    @Mock
    SimpleMessageTemplate messageTemplate;

    @Spy
    FraudDetectProperties fraudDetectProperties = new FraudDetectProperties();

    @InjectMocks
    FraudDetectRequestEventListener listener;

    @Test
    void onEvent() {
        FraudDetectRequestEvent event = new FraudDetectRequestEvent( "1234", new Transaction());
        listener.onEvent(event);
        verify(messageTemplate).send(eq(fraudDetectProperties.getRequestQueue()), eq(event));
    }
}