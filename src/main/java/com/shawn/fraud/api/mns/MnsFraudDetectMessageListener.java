package com.shawn.fraud.api.mns;


import com.aliyun.mns.model.Message;
import com.aliyun.mns.model.MessagePropertyValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shawn.fraud.api.AbstractFraudDetectEndpoint;
import com.shawn.fraud.application.CommandHandler;
import com.shawn.fraud.application.detect.FraudDetectCommand;
import com.shawn.fraud.application.detect.FraudDetectCommandResult;
import com.shawn.fraud.domain.SimpleMessageTemplate;
import com.shawn.fraud.domain.config.FraudDetectProperties;
import com.shawn.fraud.domain.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;

/**
 * accept the mns message for fraud detect request
 * mns is a simple queue provided by aliyun
 */
@Component
@Slf4j
public class MnsFraudDetectMessageListener extends AbstractFraudDetectEndpoint implements SmartLifecycle {

    private final ObjectMapper objectMapper;
    private final SimpleMessageTemplate<Message> requestMessageTemplate;
    private final SimpleMessageTemplate<Message> dltMessageTemplate;

    public MnsFraudDetectMessageListener(CommandHandler<FraudDetectCommand, FraudDetectCommandResult> commandHandler,
                                         @Qualifier(SimpleMessageTemplate.MESSAGE_TEMPLATE_REQUEST)
                                         SimpleMessageTemplate<Message> requestMessageTemplate,
                                         @Qualifier(SimpleMessageTemplate.MESSAGE_TEMPLATE_DLT)
                                         SimpleMessageTemplate<Message> dltMessageTemplate,
                                         ObjectMapper objectMapper) {
        super(commandHandler);
        this.objectMapper = objectMapper;
        this.requestMessageTemplate = requestMessageTemplate;
        this.dltMessageTemplate = dltMessageTemplate;
    }

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean running = false;


    @Override
    public void start() {
        running = true;
        executor.submit(this::doPullingLoop);
        log.info("success setup the mns message listener");
    }

    @Override
    public void stop() {
        running = false;
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
        log.info("success shutdown the mns message listener");
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    private void doPullingLoop() {
        while (running) {
           doPulling();
        }
    }

    protected void doPulling() {
        try {
            List<Message> messages = requestMessageTemplate.list();
            log.info("there are {} messages found", messages.size());
            for (Message message : messages) {
                processMessage(message);
            }
        } catch (Exception e) {
            log.error("fail to process the pulling action", e);
            // sleep briefly to avoid tight error loop
            try {
                sleep(100);
            } catch (InterruptedException ignored) {
                // do nothing
            }
        }
    }

    private void processMessage(Message message) {
        String requestId = message.getRequestId();
        try {
            Transaction transaction = objectMapper.readValue(message.getMessageBodyAsRawString(), Transaction.class);
            MessagePropertyValue replyModel = message.getUserProperties().get(FraudDetectProperties.X_REPLY_ASYNC);
            boolean replyAsync = replyModel != null && Boolean.parseBoolean(replyModel.getStringValue());
            FraudDetectCommand command = new FraudDetectCommand(requestId, transaction, replyAsync);
            commandHandler.execute(command);
        } catch (Exception exception) {
            log.warn("some exception happen on {}, resend it to dlt", requestId, exception);
            sendToDLQ(message);
        } finally {
            deleteMessage(message);
        }
    }


    private void deleteMessage(Message message){
        requestMessageTemplate.delete(message);
    }

    private void sendToDLQ(Message message) {
        Message dltMessage = new Message();
        dltMessage.setMessageBodyAsRawString(message.getMessageBodyAsRawString());
        dltMessage.setRequestId(message.getRequestId());
        dltMessageTemplate.send(message.getRequestId(), message);
        log.info("the message is send send dlt for {}", message.getRequestId());
    }

    /**
     * stop at last
     */
    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }
}
