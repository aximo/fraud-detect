package com.shawn.fraud.domain;

import lombok.Getter;

@Getter
public class FraudException extends RuntimeException {
    private final FraudError error;
    public FraudException(FraudError error) {
        super("the fraud failed, reason:" + error.name());
        this.error = error;
    }

}
