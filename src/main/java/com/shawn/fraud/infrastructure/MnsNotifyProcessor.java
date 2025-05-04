package com.shawn.fraud.infrastructure;

import com.aliyun.mns.model.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shawn.fraud.domain.FraudDetectResultNotifyProcessor;
import com.shawn.fraud.domain.FraudError;
import com.shawn.fraud.domain.SimpleMessageTemplate;
import com.shawn.fraud.domain.model.Transaction;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class MnsNotifyProcessor implements FraudDetectResultNotifyProcessor {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ObjectMapper objectMapper;
    private final SimpleMessageTemplate<Message> replyMessageTemplate;

    public MnsNotifyProcessor(
            @Qualifier(SimpleMessageTemplate.MESSAGE_TEMPLATE_RESPONSE)
            SimpleMessageTemplate<Message> replyMessageTemplate,
            ObjectMapper objectMapper) {
        this.replyMessageTemplate = replyMessageTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void notify(Transaction transaction, FraudError error) {
        String payload = null;
        try {
            payload = objectMapper.writeValueAsString(new FraudDetectResponseMessage(transaction.getId(), error != null, error));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        Message message = new Message();
        message.setRequestId(transaction.getId());
        message.setMessageBodyAsRawString(payload);
        replyMessageTemplate.send(transaction.getId(), message);
        logger.info("success send the response for transaction {}", transaction.getId());
    }

    @Data
    @AllArgsConstructor
    public static class FraudDetectResponseMessage {
        private String id;
        private boolean success;
        private FraudError error;
    }
}
