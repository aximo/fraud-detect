package com.shawn.fraud.infrastructure.mns;

/**
 * use this provider to make test easy
 */
public interface QueueProvider {
    QueueFacade withQueue(String queueName);
}
