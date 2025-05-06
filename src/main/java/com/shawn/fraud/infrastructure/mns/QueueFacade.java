package com.shawn.fraud.infrastructure.mns;

import com.aliyun.mns.common.ServiceHandlingRequiredException;
import com.aliyun.mns.model.Message;

import java.util.List;

/**
 * use this as Mns CloudQueue is hard to mock
 */
public interface QueueFacade {
    void delete(String reference) throws ServiceHandlingRequiredException;

    void putMessage(Message message);

    List<Message> batchPopMessage(int maxSize, int waitTimeSeconds) throws ServiceHandlingRequiredException;
}
