package com.shawn.fraud.api.mns;


import com.shawn.fraud.application.CommandHandler;
import com.shawn.fraud.application.detect.FraudDetectCommand;
import com.shawn.fraud.application.detect.FraudDetectCommandResult;
import com.shawn.fraud.domain.SimpleMessageTemplate;
import com.shawn.fraud.domain.config.FraudDetectProperties;
import com.shawn.fraud.domain.event.FraudDetectRequestEvent;
import com.shawn.fraud.domain.model.MessageWrapper;
import lombok.extern.slf4j.Slf4j;
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
public class MnsFraudDetectMessageListener implements SmartLifecycle {
    private final SimpleMessageTemplate messageTemplate;
    private final CommandHandler<FraudDetectCommand, FraudDetectCommandResult> commandHandler;

    private final FraudDetectProperties fraudDetectProperties;

    public MnsFraudDetectMessageListener(CommandHandler<FraudDetectCommand, FraudDetectCommandResult> commandHandler,
                                         SimpleMessageTemplate messageTemplate,
                                         FraudDetectProperties fraudDetectProperties) {
        this.commandHandler = commandHandler;
        this.messageTemplate = messageTemplate;
        this.fraudDetectProperties = fraudDetectProperties;
    }

    private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> new Thread(runnable, "mns-pull-thread"));
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
            List<MessageWrapper<FraudDetectRequestEvent>> messages = messageTemplate.list(fraudDetectProperties.getRequestQueue(), FraudDetectRequestEvent.class);
            log.info("there are {} events found in {}", messages.size(), fraudDetectProperties.getRequestQueue());
            for (MessageWrapper<FraudDetectRequestEvent> message : messages) {
                process(message);
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

    private void process(MessageWrapper<FraudDetectRequestEvent> message) {
        String requestId = message.getPayload().getRequestId();
        if (message.getPayload() == null) {
            // maybe the message is in an ill format, can not decode, in this case, directly delete from source queue
            messageTemplate.send(fraudDetectProperties.getDeadQueue(), message.getOriginContent());
            log.info("move ill message to dlt queue {}", fraudDetectProperties.getDeadQueue());
            messageTemplate.delete(fraudDetectProperties.getRequestQueue(), message.getReference());
            log.info("direct remove ill message from source queue {}", fraudDetectProperties.getRequestQueue());

            return;
        }
        try {
            FraudDetectCommand command = new FraudDetectCommand(requestId, message.getPayload().getTransaction(), false);
            commandHandler.execute(command);
        } catch (Exception exception) {
            log.warn("some exception happen on {}, resend it to dlt", requestId, exception);
            messageTemplate.send(fraudDetectProperties.getDeadQueue(), message.getPayload());
            log.info("success send the message to dlt {} for {}", fraudDetectProperties.getDeadQueue(), message.getPayload().getRequestId());
        } finally {
            messageTemplate.delete(fraudDetectProperties.getRequestQueue(), message.getReference());
            log.info("remove the request message from {} for {}", fraudDetectProperties.getRequestQueue(), message.getPayload().getRequestId());
        }
    }

    /**
     * stop at last
     */
    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }
}
