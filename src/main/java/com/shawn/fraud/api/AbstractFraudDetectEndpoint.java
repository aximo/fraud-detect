package com.shawn.fraud.api;

import com.shawn.fraud.application.CommandHandler;
import com.shawn.fraud.application.detect.FraudDetectCommand;
import com.shawn.fraud.application.detect.FraudDetectCommandResult;
import com.shawn.fraud.domain.model.Transaction;

import java.math.BigDecimal;

public class AbstractFraudDetectEndpoint {
    protected final CommandHandler<FraudDetectCommand, FraudDetectCommandResult> commandHandler;

    protected AbstractFraudDetectEndpoint(CommandHandler<FraudDetectCommand, FraudDetectCommandResult> commandHandler) {
        this.commandHandler = commandHandler;
    }

    /**
     * maybe exception happen as request body is invalid, just let the framework to show the error logs
     * no necessary handle by our-self
     */
    protected Transaction convert(FraudDetectRequest message) {
        Transaction transaction = new Transaction();
        transaction.setAge(message.getAge());
        transaction.setId(message.getId());
        transaction.setCountry(message.getCountry());
        transaction.setAmount(new BigDecimal(message.getAmount()));
        return transaction;
    }
}
