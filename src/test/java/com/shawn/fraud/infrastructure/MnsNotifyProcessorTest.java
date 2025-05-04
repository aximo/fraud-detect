package com.shawn.fraud.infrastructure;

import com.aliyun.mns.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shawn.fraud.domain.FraudError;
import com.shawn.fraud.domain.SimpleMessageTemplate;
import com.shawn.fraud.domain.model.Transaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MnsNotifyProcessorTest {

    @Mock
    SimpleMessageTemplate<Message> replyMessageTemplate;

    @Spy
    ObjectMapper objectMapper;

    @InjectMocks
    MnsNotifyProcessor notifyProcessor;

    @Test
    void testNotify() {

        Transaction transaction = new Transaction();
        transaction.setId("1234");
        transaction.setAmount(BigDecimal.valueOf(10));
        transaction.setCountry("china");
        transaction.setAge(20);

        notifyProcessor.notify(transaction, FraudError.TOO_BIG_AMOUNT);
        verify(replyMessageTemplate).send(eq(transaction.getId()), any());
    }
}