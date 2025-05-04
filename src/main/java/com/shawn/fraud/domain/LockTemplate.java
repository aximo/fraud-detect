package com.shawn.fraud.domain;

import java.util.function.Supplier;

public interface LockTemplate {
    public <T> T execute(String lockKey, Supplier<T> task);

    public <T> T execute(String lockKey, int maxRetries, int waitTime, int retryIntervalMillSeconds, Supplier<T> task);
}
