package com.shawn.fraud.api.mns;

import com.shawn.fraud.application.CommandHandler;
import com.shawn.fraud.application.detect.FraudDetectCommand;
import com.shawn.fraud.application.detect.FraudDetectCommandResult;
import com.shawn.fraud.domain.SimpleMessageTemplate;
import com.shawn.fraud.domain.config.FraudDetectProperties;
import com.shawn.fraud.domain.event.FraudDetectRequestEvent;
import com.shawn.fraud.domain.model.MessageWrapper;
import com.shawn.fraud.domain.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MnsFraudDetectMessageListenerTest {
    @Mock
    SimpleMessageTemplate messageTemplate;

    @Spy
    FraudDetectProperties fraudDetectProperties = new FraudDetectProperties();

    @Mock
    CommandHandler<FraudDetectCommand, FraudDetectCommandResult> commandHandler;

    @InjectMocks
    MnsFraudDetectMessageListener mnsFraudDetectMessageListener;

    @BeforeEach
    void setup() {
        clearInvocations(messageTemplate, commandHandler);
    }

    @Test
    void start() {
        mnsFraudDetectMessageListener.start();
        assertTrue(mnsFraudDetectMessageListener.isRunning());
        mnsFraudDetectMessageListener.stop();
        assertFalse(mnsFraudDetectMessageListener.isRunning());
        assertEquals(Integer.MAX_VALUE, mnsFraudDetectMessageListener.getPhase());
        verify(commandHandler, never()).execute(any());


    }

    @Test
    void pulling_happy_case() {
        List<MessageWrapper<FraudDetectRequestEvent>> messages = buildMessages("1234");
        when(messageTemplate.list(fraudDetectProperties.getRequestQueue(), FraudDetectRequestEvent.class)).thenReturn(messages);
        when(commandHandler.execute(argThat(argument ->
                argument != null && argument.getRequestId().equalsIgnoreCase("1234")))
        ).thenReturn(FraudDetectCommandResult.success());

        mnsFraudDetectMessageListener.doPulling();
        verify(commandHandler, times(1)).execute(any());

        // should delete the message
        verify(messageTemplate, times(1)).delete(
                fraudDetectProperties.getRequestQueue(),
                "ref-1234");

        // should not send to dlt as it is successful
        verify(messageTemplate, never()).send(eq(fraudDetectProperties.getDeadQueue()), any());
    }


    @Test
    void pulling_error_case() {
        List<MessageWrapper<FraudDetectRequestEvent>> messages = buildMessages("1235");
        when(messageTemplate.list(fraudDetectProperties.getRequestQueue(), FraudDetectRequestEvent.class)).thenReturn(messages);
        when(commandHandler.execute(argThat(argument ->
                argument != null && argument.getRequestId().equalsIgnoreCase("1235")))
        ).thenThrow(RuntimeException.class);

        mnsFraudDetectMessageListener.doPulling();
        verify(commandHandler, times(1)).execute(any());

        // should delete the message
        verify(messageTemplate, times(1)).delete(
                fraudDetectProperties.getRequestQueue(),
                "ref-1235");
        // should be sent to dlt
        verify(messageTemplate, never()).delete(
                fraudDetectProperties.getDeadQueue(),
                "ref-1235");

        verify(messageTemplate, times(1)).send(eq(fraudDetectProperties.getDeadQueue()), any());
    }


    private List<MessageWrapper<FraudDetectRequestEvent>> buildMessages(String id) {
        List<MessageWrapper<FraudDetectRequestEvent>> messages = new ArrayList<>();
        messages.add(buildMessage(id));
        return messages;
    }

    private MessageWrapper<FraudDetectRequestEvent> buildMessage(String id) {
        Transaction transaction = new Transaction();
        transaction.setAmount(BigDecimal.valueOf(100));
        transaction.setAge(20);
        transaction.setCountry("china");
        transaction.setId(id);
        FraudDetectRequestEvent fraudDetectRequestEvent = new FraudDetectRequestEvent(id, transaction);
        return new MessageWrapper<>(UUID.randomUUID().toString(), fraudDetectRequestEvent, "ref-" + id, null);
    }

}