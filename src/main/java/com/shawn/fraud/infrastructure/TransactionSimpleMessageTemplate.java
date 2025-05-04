package com.shawn.fraud.infrastructure;

import com.aliyun.mns.client.CloudQueue;
import com.aliyun.mns.common.ServiceHandlingRequiredException;
import com.aliyun.mns.model.Message;
import com.shawn.fraud.domain.SimpleMessageTemplate;
import com.shawn.fraud.domain.config.FraudDetectProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.List;

@Slf4j
public class TransactionSimpleMessageTemplate implements SimpleMessageTemplate<Message> {
    private final CloudQueue queue;

    private final FraudDetectProperties fraudDetectProperties;

    public TransactionSimpleMessageTemplate(CloudQueue queue, FraudDetectProperties fraudDetectProperties) {
        this.queue = queue;
        this.fraudDetectProperties = fraudDetectProperties;
    }

    @Override
    public void send(String requestId, Message message) {
        Assert.isTrue(requestId.equals(message.getMessageId()), () -> "the request id " + requestId + " must be same with the one in Message but " + message.getMessageId());
        queue.putMessage(message);
    }

    @Override
    public void delete(Message message) {
        try {
            queue.deleteMessage(message.getReceiptHandle());
        } catch (ServiceHandlingRequiredException e) {
            throw new IllegalCallerException("can not delete the message " + message.getRequestId(), e);
        }
    }

    @Override
    public List<Message> list(int maxSize) {
        try {
            List<Message> messages = queue.batchPopMessage(maxSize, fraudDetectProperties.getWaitTimeSeconds());
            return messages == null ? Collections.emptyList() : messages;
        } catch (Exception exception) {
            log.warn("can not fetch the messages from {}", queue.getQueueURL());
            return Collections.emptyList();
        }


    }

    @Override
    public List<Message> list() {
        return list(fraudDetectProperties.getMaxNumberOfMessages());
    }
}
