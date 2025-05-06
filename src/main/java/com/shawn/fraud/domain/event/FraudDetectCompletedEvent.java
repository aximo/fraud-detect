package com.shawn.fraud.domain.event;

import com.shawn.fraud.domain.FraudError;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FraudDetectCompletedEvent {
    private boolean success;
    private FraudError error;
    private String requestId;
    private String transactionId;

}
