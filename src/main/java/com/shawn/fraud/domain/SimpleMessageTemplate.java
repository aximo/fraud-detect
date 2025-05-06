package com.shawn.fraud.domain;

import com.shawn.fraud.domain.model.MessageWrapper;

import java.util.List;

public interface SimpleMessageTemplate {
    public <T> void send(String queue, T message);

    public void delete(String queue, String reference);

    public <T> List<MessageWrapper<T>> list(String queue, Class<T> payloadClazz, int maxSize);

    public <T> List<MessageWrapper<T>> list(String queue, Class<T> payloadClazz);
}
