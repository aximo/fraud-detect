package com.shawn.fraud.api;

import com.shawn.fraud.domain.FraudError;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * demonstrate the api layer response struct is independent with domain model
 * this request should be immutable
 */
@Data
@AllArgsConstructor
public class FraudDetectResponse {
    private final boolean success;
    private final FraudError error;
}
