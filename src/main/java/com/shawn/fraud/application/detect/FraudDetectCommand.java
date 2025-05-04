package com.shawn.fraud.application.detect;

import com.shawn.fraud.application.Command;
import com.shawn.fraud.domain.model.Transaction;
import lombok.Getter;

@Getter
public class FraudDetectCommand implements Command {
    private final String requestId;
    private final Transaction transaction;

    private boolean async;

    public FraudDetectCommand(String requestId,Transaction transaction, boolean async) {
        this.transaction = transaction;
        this.async = async;
        this.requestId = requestId;
    }
}
