package com.shawn.fraud.application.detect;

import com.shawn.fraud.domain.SimpleMessageTemplate;
import com.shawn.fraud.domain.config.FraudDetectProperties;
import com.shawn.fraud.domain.event.FraudDetectCompletedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FraudDetectCompletedEventListenerTest {

    @Mock
    SimpleMessageTemplate messageTemplate;

    @Spy
    FraudDetectProperties fraudDetectProperties = new FraudDetectProperties();

    @InjectMocks
    FraudDetectCompletedEventListener listener;

    @Test
    void onEvent() {
        FraudDetectCompletedEvent event = new FraudDetectCompletedEvent(true, null, "1234", "1234");
        listener.onEvent(event);
        verify(messageTemplate).send(eq(fraudDetectProperties.getResponseQueue()), eq(event));
    }
}