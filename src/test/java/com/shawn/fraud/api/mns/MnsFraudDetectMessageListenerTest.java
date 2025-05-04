package com.shawn.fraud.api.mns;

import com.aliyun.mns.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shawn.fraud.application.CommandHandler;
import com.shawn.fraud.application.detect.FraudDetectCommand;
import com.shawn.fraud.application.detect.FraudDetectCommandResult;
import com.shawn.fraud.domain.SimpleMessageTemplate;
import com.shawn.fraud.domain.model.Transaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.net.ConnectException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MnsFraudDetectMessageListenerTest {
    @Mock
    SimpleMessageTemplate<Message> requestMessageTemplate;

    @Mock
    SimpleMessageTemplate<Message> dltMessageTemplate;

    @Spy
    ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    CommandHandler<FraudDetectCommand, FraudDetectCommandResult> commandHandler;

    @InjectMocks
    MnsFraudDetectMessageListener mnsFraudDetectMessageListener;

    @Test
    void start() throws Exception {
        mnsFraudDetectMessageListener.start();
        assertTrue(mnsFraudDetectMessageListener.isRunning());
        mnsFraudDetectMessageListener.stop();
        assertFalse(mnsFraudDetectMessageListener.isRunning());
        assertEquals(Integer.MAX_VALUE, mnsFraudDetectMessageListener.getPhase());
        verify(commandHandler, never()).execute(any());

        List<Message> messages = buildMessages();
        when(requestMessageTemplate.list()).thenReturn(messages);
        when(commandHandler.execute(argThat(argument ->
                argument != null && argument.getRequestId().equalsIgnoreCase("1234")))
        ).thenReturn(FraudDetectCommandResult.success());

        when(commandHandler.execute(argThat(argument ->
                argument != null && argument.getRequestId().equalsIgnoreCase("1235")))
        ).thenThrow(RuntimeException.class);

        mnsFraudDetectMessageListener.doPulling();
        verify(commandHandler, times(2)).execute(any());
        verify(requestMessageTemplate, times(2)).delete(any());
        verify(requestMessageTemplate, times(1)).send(any(), any());
    }

    private List<Message> buildMessages() throws Exception {
        List<Message> messages = new ArrayList<>();
        Message successMessage = new Message();
        successMessage.setRequestId("1234");
        Transaction transaction = new Transaction();
        transaction.setAmount(BigDecimal.valueOf(100));
        transaction.setAge(20);
        transaction.setCountry("china");
        transaction.setId(successMessage.getRequestId());
        successMessage.setMessageBodyAsRawString(objectMapper.writeValueAsString(transaction));

        Message failMessage = new Message();
        failMessage.setRequestId("1235");
        transaction.setId(failMessage.getRequestId());
        failMessage.setMessageBodyAsRawString(objectMapper.writeValueAsString(transaction));

        messages.add(successMessage);
        messages.add(failMessage);
        return messages;
    }

}