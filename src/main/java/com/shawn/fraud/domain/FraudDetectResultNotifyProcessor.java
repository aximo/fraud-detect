package com.shawn.fraud.domain;

import com.shawn.fraud.domain.model.Transaction;

public interface FraudDetectResultNotifyProcessor {

    public void notify(Transaction transaction, FraudError error);
}
