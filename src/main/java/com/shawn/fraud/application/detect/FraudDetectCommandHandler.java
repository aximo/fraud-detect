package com.shawn.fraud.application.detect;

import com.shawn.fraud.application.CommandHandler;
import com.shawn.fraud.domain.FraudException;
import com.shawn.fraud.domain.FraudService;
import com.shawn.fraud.domain.LockTemplate;
import com.shawn.fraud.domain.config.FraudDetectProperties;
import com.shawn.fraud.domain.event.FraudDetectResultEvent;
import com.shawn.fraud.domain.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * command handler use to handle business flow, in most case, it will use domain object for process
 */
@Component
public class FraudDetectCommandHandler implements CommandHandler<FraudDetectCommand, FraudDetectCommandResult> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final FraudService service;

    private final ApplicationEventPublisher applicationEventPublisher;

    private final LockTemplate lockTemplate;

    private final RedisTemplate<String, FraudDetectCommandResult> redisTemplate;

    private final FraudDetectProperties fraudDetectProperties;

    public FraudDetectCommandHandler(FraudDetectProperties fraudDetectProperties,
                                     FraudService service,
                                     LockTemplate lockTemplate,
                                     RedisTemplate<String, FraudDetectCommandResult> redisTemplate,
                                     ApplicationEventPublisher applicationEventPublisher) {
        this.service = service;
        this.lockTemplate = lockTemplate;
        this.applicationEventPublisher = applicationEventPublisher;
        this.redisTemplate = redisTemplate;
        this.fraudDetectProperties = fraudDetectProperties;
    }

    @Override
    public FraudDetectCommandResult execute(FraudDetectCommand command) {
        FraudDetectCommandResult cacheResult = redisTemplate.opsForValue().get(buildCacheKey(command));
        if (cacheResult != null) {
            return cacheResult;
        }
        return lockTemplate.execute("fraud/detect/" + command.getRequestId(), () -> this.process(command));
    }

    private String buildCacheKey(FraudDetectCommand command) {
        return "fraud/detect/response/" + command.getRequestId();
    }


    private FraudDetectCommandResult process(FraudDetectCommand command) {
        Assert.notNull(command.getTransaction(), () -> "the transaction should not be empty");
        Transaction transaction = command.getTransaction();
        logger.info("will process the fraud detect command, transactionId={}, amount={}, age={}, countory={}", transaction.getId(), transaction.getAmount(), transaction.getAge(), transaction.getCountry());
        try {
            service.detect(transaction);
            logger.debug("success process the fraud detect for transaction={}", transaction.getId());
            FraudDetectCommandResult result = FraudDetectCommandResult.success();
            postExecute(command, result);
            return result;
        } catch (FraudException exception) {
            logger.warn("fraud detect fail for {} as {}", transaction.getId(), exception.getMessage());
            FraudDetectCommandResult result = FraudDetectCommandResult.fail(exception.getError());
            postExecute(command, result);
            return result;
        }
    }

    /**
     * should send an event for detect result, in sync case, we should give result notify
     */
    private void postExecute(FraudDetectCommand command, FraudDetectCommandResult result) {
        redisTemplate.opsForValue().set(buildCacheKey(command), result, fraudDetectProperties.getTtl());
        FraudDetectResultEvent event = new FraudDetectResultEvent(
                command.isAsync(),
                command.getTransaction(),
                result.isSuccess(),
                result.getError());
        applicationEventPublisher.publishEvent(event);
    }
}
