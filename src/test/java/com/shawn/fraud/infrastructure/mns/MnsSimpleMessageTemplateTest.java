package com.shawn.fraud.infrastructure.mns;

import com.aliyun.mns.common.ServiceHandlingRequiredException;
import com.aliyun.mns.model.Message;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shawn.fraud.domain.config.FraudDetectProperties;
import com.shawn.fraud.domain.event.FraudDetectCompletedEvent;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MnsSimpleMessageTemplateTest {

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private QueueProvider queueProvider;
    @Spy
    private FraudDetectProperties fraudDetectProperties = new FraudDetectProperties();

    @InjectMocks
    MnsSimpleMessageTemplate mnsSimpleMessageTemplate;

    private final QueueFacade queueFacade = mock(QueueFacade.class);
    private final String queue = "test";

    @BeforeEach
    void setup() throws Exception {
        clearInvocations(queueProvider, queueFacade, objectMapper);
        doReturn(queueFacade).when(queueProvider).withQueue(queue);
        List<Message> messages = buildMessages();
        doReturn(messages).when(queueFacade).batchPopMessage(anyInt(), anyInt());
    }

    private List<Message> buildMessages() throws Exception {
        List<Message> messages = new ArrayList<>();
        Message message = new Message();
        FraudDetectRequestEvent event = new FraudDetectRequestEvent();
        event.setRequestId("1234");
        Transaction transaction = new Transaction();
        transaction.setId("1234");
        transaction.setAge(20);
        transaction.setCountry("china");
        transaction.setAmount(BigDecimal.ONE);
        event.setTransaction(transaction);
        message.setMessageBodyAsRawString(objectMapper.writeValueAsString(event));
        message.setReceiptHandle("ref-1234");
        messages.add(message);
        return messages;
    }

    @Test
    void send() {
        FraudDetectCompletedEvent fraudDetectCompletedEvent = new FraudDetectCompletedEvent(true, null, "1234", "1234");
        mnsSimpleMessageTemplate.send(queue, fraudDetectCompletedEvent);
        verify(queueFacade, times(1)).putMessage(any());
    }

    @Test
    void delete() throws Exception {
        mnsSimpleMessageTemplate.delete(queue, "ref-1234");
        verify(queueFacade, times(1)).delete("ref-1234");
    }

    @Test
    void delete_with_error() throws Exception {
        doThrow(new ServiceHandlingRequiredException()).when(queueFacade).delete("ref-1234");
        assertThrows(RuntimeException.class, () -> {
            mnsSimpleMessageTemplate.delete(queue, "ref-1234");
        });
    }

    @Test
    void list_with_params() {
        List<MessageWrapper<FraudDetectRequestEvent>> messages = mnsSimpleMessageTemplate.list(queue, FraudDetectRequestEvent.class, 100);
        assertEquals(1, messages.size());
        MessageWrapper<FraudDetectRequestEvent> message = messages.get(0);
        assertEquals("ref-1234", message.getReference());
        assertEquals("1234", message.getPayload().getRequestId());
        assertEquals(20, message.getPayload().getTransaction().getAge());
    }

    @Test
    void list_with_net_error() throws Exception {
        doThrow(new ServiceHandlingRequiredException()).when(queueFacade).batchPopMessage(anyInt(), anyInt());
        List<MessageWrapper<FraudDetectRequestEvent>> messages = mnsSimpleMessageTemplate.list(queue, FraudDetectRequestEvent.class, 100);
        assertTrue(messages.isEmpty());
    }

    @Test
    void list_with_mapping_error() throws Exception {
        doThrow(new JsonParseException(null, "mock")).when(objectMapper).readValue(anyString(), eq(FraudDetectRequestEvent.class));
        List<MessageWrapper<FraudDetectRequestEvent>> messages = mnsSimpleMessageTemplate.list(queue, FraudDetectRequestEvent.class, 100);
        assertFalse(messages.isEmpty());
        assertNull(messages.get(0).getPayload());
        assertEquals("ref-1234", messages.get(0).getReference());
    }

    @Test
    void list_without_params() {
        List<MessageWrapper<FraudDetectRequestEvent>> messages = mnsSimpleMessageTemplate.list(queue, FraudDetectRequestEvent.class);
        assertEquals(1, messages.size());
        MessageWrapper<FraudDetectRequestEvent> message = messages.get(0);
        assertEquals("ref-1234", message.getReference());
        assertEquals("1234", message.getPayload().getRequestId());
        assertEquals(20, message.getPayload().getTransaction().getAge());
    }
}