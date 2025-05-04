package com.shawn.fraud.application.detect;

import com.shawn.fraud.domain.FraudDetectResultNotifyProcessor;
import com.shawn.fraud.domain.event.FraudDetectResultEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class FraudDetectResultEventListener {
    private final FraudDetectResultNotifyProcessor processor;

    public FraudDetectResultEventListener(FraudDetectResultNotifyProcessor processor) {
        this.processor = processor;
    }

    /**
     * async process the result to improve performance, we should take care of graceful shutdown
     */
    @EventListener
    public void onEvent(FraudDetectResultEvent event) {
        if (event.isAsync()) {
            processor.notify(event.getTransaction(), event.isSuccess() ? null : event.getError());
        }
    }

}
