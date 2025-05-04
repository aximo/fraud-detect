package com.shawn.fraud.application.detect;

import com.shawn.fraud.domain.FraudDetectResultNotifyProcessor;
import com.shawn.fraud.domain.event.FraudDetectResultEvent;
import com.shawn.fraud.domain.model.Transaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FraudDetectResultEventListenerTest {

    @Mock
    FraudDetectResultNotifyProcessor processor;

    @InjectMocks
    FraudDetectResultEventListener listener;

    @Test
    void onEvent_when_async() {
        Transaction transaction = new Transaction();
        transaction.setCountry("china");
        transaction.setId(UUID.randomUUID().toString());
        transaction.setAge(20);
        transaction.setAmount(BigDecimal.valueOf(100));
        FraudDetectResultEvent event = new FraudDetectResultEvent(true, transaction, true, null);

        listener.onEvent(event);
        verify(processor).notify(transaction, null);
    }

    @Test
    void onEvent_when_not_async() {
        Transaction transaction = new Transaction();
        transaction.setCountry("china");
        transaction.setId(UUID.randomUUID().toString());
        transaction.setAge(20);
        transaction.setAmount(BigDecimal.valueOf(100));
        FraudDetectResultEvent event = new FraudDetectResultEvent(false, transaction, true, null);

        listener.onEvent(event);
        verify(processor, never()).notify(any(),any());
    }
}