package com.shawn.fraud.domain.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "fraud.detect")
@Data
public class FraudDetectProperties {
    public static final String X_REQUEST_ID = "x-request-id";
    /**
     * the value should be true or false
     */
    public static final String X_REPLY_ASYNC = "x-reply-async";

    private String requestQueue = "fraud-detect-request";
    private String responseQueue = "fraud-detect-response";
    private String deadQueue = "fraud-detect-dlt";
    /**
     * how many messages will return every fetch
     */
    private int maxNumberOfMessages = 16;

    /**
     * if wait N seconds,there still no message found, return.
     * if too slow, it will return empty frequently
     */
    private int waitTimeSeconds = 10;

    /**
     * if one node fetch the message, it will keep messagem un-visibility in N seconds.
     * so other node will not process the message duplicate.
     */
    private int visibilityTimeoutSeconds = 10;

    /**
     * the result ttl for fraud result cache
     */
    private Duration ttl = Duration.ofDays(1);
}
