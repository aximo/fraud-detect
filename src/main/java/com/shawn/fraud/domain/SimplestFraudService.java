package com.shawn.fraud.domain;

import com.shawn.fraud.domain.model.Transaction;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 *
 */
@Component
public class SimplestFraudService implements FraudService {
    @Override
    public void detect(Transaction transaction) throws FraudException {
        if (transaction.getAge() < 14) {
            throw new FraudException(FraudError.TOO_YOUNG);
        }

        if (transaction.getAge() < 20 && transaction.getAmount().compareTo(BigDecimal.valueOf(1000)) > 0) {
            throw new FraudException(FraudError.TOO_BIG_AMOUNT);
        }

        if (transaction.getCountry() == null || !transaction.getCountry().equalsIgnoreCase("china")) {
            throw new FraudException(FraudError.COUNTRY_NOT_SUPPORTED);
        }

        if (transaction.getAmount().compareTo(BigDecimal.valueOf(10000)) > 0) {
            throw new FraudException(FraudError.TOO_BIG_AMOUNT);
        }
        // ok pass it
    }
}
