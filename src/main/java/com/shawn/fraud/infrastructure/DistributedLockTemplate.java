package com.shawn.fraud.infrastructure;

import com.shawn.fraud.domain.LockTemplate;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Component
public class DistributedLockTemplate implements LockTemplate {

    private final RedissonClient redissonClient;

    public DistributedLockTemplate(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public <T> T execute(String lockKey, Supplier<T> task) {
        return execute(lockKey, 3, 5, 1000, task);
    }

    public <T> T execute(String lockKey, int maxRetries, int waitTime,int retryIntervalMillSeconds, Supplier<T> task) {
        RLock lock = redissonClient.getLock(lockKey);
        int attempt = 0;

        while (attempt <= maxRetries) {
            try {
                boolean acquired = lock.tryLock(waitTime, TimeUnit.SECONDS);
                if (acquired) {
                    try {
                        return task.get();
                    } finally {
                        if (lock.isHeldByCurrentThread()) {
                            lock.unlock();
                        }
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while trying to acquire lock", e);
            } catch (Exception e) {
                throw new RuntimeException("Error while executing task with lock", e);
            }

            attempt++;
            try {
                TimeUnit.MILLISECONDS.sleep(retryIntervalMillSeconds); // just wait some time for next retry
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        throw new IllegalStateException("Failed to acquire lock after " + maxRetries + " retries.");
    }
}
