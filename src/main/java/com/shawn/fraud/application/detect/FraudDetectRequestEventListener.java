package com.shawn.fraud.application.detect;

import com.shawn.fraud.domain.SimpleMessageTemplate;
import com.shawn.fraud.domain.config.FraudDetectProperties;
import com.shawn.fraud.domain.event.FraudDetectRequestEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class FraudDetectRequestEventListener {
    private final SimpleMessageTemplate messageTemplate;
    private final FraudDetectProperties fraudDetectProperties;

    public FraudDetectRequestEventListener(SimpleMessageTemplate messageTemplate, FraudDetectProperties fraudDetectProperties) {
        this.messageTemplate = messageTemplate;
        this.fraudDetectProperties = fraudDetectProperties;
    }

    @EventListener
    public void onEvent(FraudDetectRequestEvent event) {
        messageTemplate.send(fraudDetectProperties.getRequestQueue(), event);
    }

}
