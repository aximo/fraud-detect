package com.shawn.fraud.infrastructure.mns;

import com.aliyun.mns.client.MNSClient;

public class DefaultQueueProvider implements QueueProvider {
    private final MNSClient client;

    public DefaultQueueProvider(MNSClient client) {
        this.client = client;
    }

    @Override
    public QueueFacade withQueue(String queueName) {
        return new MnsQueueFacade(client.getQueueRef(queueName));
    }
}
