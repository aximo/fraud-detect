package com.shawn.fraud.api;

import lombok.Getter;

/**
 * demonstrate the api layer request struct is different with domain model
 * this request should be immutable
 */
@Getter
public class FraudDetectRequest {

    private final String id;
    private final long amount;

    private int age = 0;
    private final String country;

    public FraudDetectRequest(String id, long amount, int age, String country) {
        this.id = id;
        this.amount = amount;
        this.age = age;
        this.country = country;
    }
}
