package com.shawn.fraud.infrastructure.mns;

import com.aliyun.mns.common.ServiceHandlingRequiredException;
import com.aliyun.mns.model.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shawn.fraud.domain.SimpleMessageTemplate;
import com.shawn.fraud.domain.config.FraudDetectProperties;
import com.shawn.fraud.domain.model.MessageWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class MnsSimpleMessageTemplate implements SimpleMessageTemplate {
    private final ObjectMapper objectMapper;
    private final QueueProvider queueProvider;
    private final FraudDetectProperties fraudDetectProperties;

    protected MnsSimpleMessageTemplate(
            QueueProvider queueProvider,
            FraudDetectProperties fraudDetectProperties,
            ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.fraudDetectProperties = fraudDetectProperties;
        this.queueProvider = queueProvider;
    }


    @Override
    public <T> void send(String queue, T payload) {
        Message message = new Message();
        try {
            message.setMessageBodyAsRawString(objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("fail to convert event to json  ", e);
        }
        queueProvider.withQueue(queue).putMessage(message);
    }

    @Override
    public void delete(String queue, String reference) {
        Assert.hasText(reference, "the message reference handle should not be empty when delete");
        try {
            queueProvider.withQueue(queue).delete(reference);
        } catch (ServiceHandlingRequiredException e) {
            throw new RuntimeException("fail to delete the message with " + reference, e);
        }
    }

    @Override
    public <T> List<MessageWrapper<T>> list(String queue, Class<T> payloadClazz, int maxSize) {
        try {
            QueueFacade cloudQueue = queueProvider.withQueue(queue);
            List<Message> messages = cloudQueue.batchPopMessage(maxSize, fraudDetectProperties.getWaitTimeSeconds());
            return messages == null ? Collections.emptyList() : messages.stream().map(it -> this.mapping(it, payloadClazz)).collect(Collectors.toList());
        } catch (Exception exception) {
            log.warn("can not fetch the messages from {}", queue, exception);
            return Collections.emptyList();
        }


    }

    private <T> MessageWrapper<T> mapping(Message message, Class<T> payloadClazz) {
        try {
            T event = objectMapper.readValue(message.getMessageBodyAsRawString(), payloadClazz);
            return new MessageWrapper<>(message.getMessageId(), event, message.getReceiptHandle(), message.getMessageBodyAsRawString());
        } catch (JsonProcessingException e) {
            // return a message wrapper with null if any decode error,so that we can move it to dlt
            return new MessageWrapper<>(message.getMessageId(), null, message.getReceiptHandle(), message.getMessageBodyAsRawString());
        }
    }


    @Override
    public <T> List<MessageWrapper<T>> list(String queue, Class<T> payloadClazz) {
        return list(queue, payloadClazz, fraudDetectProperties.getMaxNumberOfMessages());
    }
}
