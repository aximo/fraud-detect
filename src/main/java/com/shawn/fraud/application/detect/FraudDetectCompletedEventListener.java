package com.shawn.fraud.application.detect;

import com.shawn.fraud.domain.SimpleMessageTemplate;
import com.shawn.fraud.domain.config.FraudDetectProperties;
import com.shawn.fraud.domain.event.FraudDetectCompletedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FraudDetectCompletedEventListener {
    private final SimpleMessageTemplate messageTemplate;
    private final FraudDetectProperties fraudDetectProperties;

    public FraudDetectCompletedEventListener(SimpleMessageTemplate messageTemplate, FraudDetectProperties fraudDetectProperties) {
        this.messageTemplate = messageTemplate;
        this.fraudDetectProperties = fraudDetectProperties;
    }

    /**
     * async process the result to improve performance, we should take care of graceful shutdown
     */
    @EventListener
    @Async
    public void onEvent(FraudDetectCompletedEvent event) {
        messageTemplate.send(fraudDetectProperties.getResponseQueue(), event);
        log.info("success send the detect completed result to queue {} for transaction {}", fraudDetectProperties.getRequestQueue(), event.getRequestId());
    }

}
