package com.shawn.fraud.infrastructure.mns;

import com.aliyun.mns.client.MNSClient;
import com.aliyun.mns.model.Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultQueueProviderTest {

    @Test
    void withQueue() {
        DefaultQueueProvider defaultQueueProvider = new DefaultQueueProvider(Mockito.mock(MNSClient.class));
        QueueFacade queueFacade = defaultQueueProvider.withQueue("test");
        Assertions.assertEquals(MnsQueueFacade.class, queueFacade.getClass());

        assertThrows(NullPointerException.class, () -> queueFacade.putMessage(new Message()));

        assertThrows(NullPointerException.class, () -> queueFacade.delete("ref-1234"));

        assertThrows(NullPointerException.class, () -> queueFacade.batchPopMessage(1, 1));


    }
}