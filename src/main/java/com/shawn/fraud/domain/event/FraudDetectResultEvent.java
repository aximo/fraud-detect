package com.shawn.fraud.domain.event;

import com.shawn.fraud.domain.FraudError;
import com.shawn.fraud.domain.model.Transaction;
import lombok.Getter;

@Getter
public class FraudDetectResultEvent {
    private boolean async;
    private final Transaction transaction;
    private final boolean success;
    private final FraudError error;

    public FraudDetectResultEvent(boolean async, Transaction transaction, boolean success, FraudError error) {
        this.async = async;
        this.transaction = transaction;
        this.success = success;
        this.error = error;
    }
}
