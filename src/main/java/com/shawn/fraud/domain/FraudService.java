package com.shawn.fraud.domain;

import com.shawn.fraud.domain.model.Transaction;

public interface FraudService {
    /**
     * @param transaction the target transaction
     * @throws FraudException if detect not pass, throw exception
     */
    public void detect(Transaction transaction) throws FraudException;
}
