package com.shawn.fraud.infrastructure.mns;

import com.aliyun.mns.client.CloudQueue;
import com.aliyun.mns.common.ServiceHandlingRequiredException;
import com.aliyun.mns.model.Message;

import java.util.List;

public class MnsQueueFacade implements QueueFacade {
    private final CloudQueue cloudQueue;

    public MnsQueueFacade(CloudQueue cloudQueue) {
        this.cloudQueue = cloudQueue;
    }

    @Override
    public void delete(String reference) throws ServiceHandlingRequiredException{
        cloudQueue.deleteMessage(reference);
    }

    @Override
    public void putMessage(Message message) {
        cloudQueue.putMessage(message);
    }

    @Override
    public List<Message> batchPopMessage(int maxSize, int waitTimeSeconds) throws ServiceHandlingRequiredException {
        return cloudQueue.batchPopMessage(maxSize, waitTimeSeconds);
    }
}
