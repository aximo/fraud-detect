package com.shawn.fraud.domain.event;

import com.shawn.fraud.domain.model.Transaction;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FraudDetectRequestEvent {
    private String requestId;
    private Transaction transaction;

    public FraudDetectRequestEvent(
            String requestId,
            Transaction transaction) {
        this.transaction = transaction;
        this.requestId = requestId;
    }
}
