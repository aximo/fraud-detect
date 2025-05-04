package com.shawn.fraud.api.restful;

import com.aliyun.mns.model.Message;
import com.aliyun.mns.model.MessagePropertyValue;
import com.aliyun.mns.model.PropertyType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shawn.fraud.api.AbstractFraudDetectEndpoint;
import com.shawn.fraud.api.FraudDetectRequest;
import com.shawn.fraud.api.FraudDetectResponse;
import com.shawn.fraud.application.detect.FraudDetectCommand;
import com.shawn.fraud.application.detect.FraudDetectCommandHandler;
import com.shawn.fraud.application.detect.FraudDetectCommandResult;
import com.shawn.fraud.domain.SimpleMessageTemplate;
import com.shawn.fraud.domain.config.FraudDetectProperties;
import com.shawn.fraud.domain.model.Transaction;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

@RequestMapping("/api/transactions")
@RestController
public class FraudDetectRestful extends AbstractFraudDetectEndpoint {
    private final SimpleMessageTemplate<Message> requestMessageTemplate;
    private final ObjectMapper objectMapper;

    public FraudDetectRestful(FraudDetectCommandHandler commandHandler,
                              @Qualifier(SimpleMessageTemplate.MESSAGE_TEMPLATE_REQUEST)
                              SimpleMessageTemplate<Message> requestMessageTemplate,
                              ObjectMapper objectMapper) {
        super(commandHandler);
        this.requestMessageTemplate = requestMessageTemplate;
        this.objectMapper = objectMapper;
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
    public void detectAsync(
            @RequestHeader(value = FraudDetectProperties.X_REQUEST_ID) String requestId,
            @RequestBody FraudDetectRequest request) throws Exception {
        Transaction transaction = new Transaction();
        transaction.setId(request.getId());
        transaction.setAge(request.getAge());
        transaction.setCountry(request.getCountry());
        transaction.setAmount(BigDecimal.valueOf(request.getAmount()));

        Message message = new Message(objectMapper.writeValueAsString(transaction));
        message.setRequestId(requestId);
        Map<String, MessagePropertyValue> userProperties = Collections.singletonMap(
                FraudDetectProperties.X_REPLY_ASYNC,
                new MessagePropertyValue(PropertyType.BOOLEAN, Boolean.TRUE.toString())
        );
        message.setUserProperties(userProperties);
        requestMessageTemplate.send(requestId, message);
    }
}
