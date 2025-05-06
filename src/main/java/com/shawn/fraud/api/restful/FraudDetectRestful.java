package com.shawn.fraud.api.restful;

import com.shawn.fraud.api.FraudDetectRequest;
import com.shawn.fraud.api.FraudDetectResponse;
import com.shawn.fraud.application.CommandHandler;
import com.shawn.fraud.application.detect.FraudDetectCommand;
import com.shawn.fraud.application.detect.FraudDetectCommandResult;
import com.shawn.fraud.domain.config.FraudDetectProperties;
import com.shawn.fraud.domain.model.Transaction;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RequestMapping("/api/transactions")
@RestController
public class FraudDetectRestful{

    private final CommandHandler<FraudDetectCommand, FraudDetectCommandResult> commandHandler;

    public FraudDetectRestful(CommandHandler<FraudDetectCommand, FraudDetectCommandResult> commandHandler) {
        this.commandHandler = commandHandler;
    }


    /**
     * get detect result directly
     */
    @PostMapping("/fraud/detect")
    public FraudDetectResponse detect(
            @RequestHeader(value = FraudDetectProperties.X_REQUEST_ID) String requestId,
            @RequestBody FraudDetectRequest request) {
        Transaction transaction = convert(request);
        FraudDetectCommand command = new FraudDetectCommand(requestId, transaction, false);
        FraudDetectCommandResult result = commandHandler.execute(command);
        return new FraudDetectResponse(result.isSuccess(), result.getError());
    }

    /**
     * do fraud detect async, will send the message to queue and return asap
     * it will send message to queue
     */
    @PostMapping("/fraud/detect/async")
    public FraudDetectResponse detectAsync(
            @RequestHeader(value = FraudDetectProperties.X_REQUEST_ID) String requestId,
            @RequestBody FraudDetectRequest request) {
        Transaction transaction = convert(request);
        FraudDetectCommand command = new FraudDetectCommand(requestId, transaction, true);
        commandHandler.execute(command);
        return new FraudDetectResponse(true, null);
    }

    /**
     * maybe exception happen as request body is invalid, just let the framework to show the error logs
     * no necessary handle by our-self
     */
    private Transaction convert(FraudDetectRequest message) {
        Transaction transaction = new Transaction();
        transaction.setAge(message.getAge());
        transaction.setId(message.getId());
        transaction.setCountry(message.getCountry());
        transaction.setAmount(new BigDecimal(message.getAmount()));
        return transaction;
    }
}
