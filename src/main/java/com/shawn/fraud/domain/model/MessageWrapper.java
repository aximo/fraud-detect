package com.shawn.fraud.domain.model;

import lombok.Getter;

import javax.annotation.Nullable;

@Getter
public class MessageWrapper<T> {
    /**
     * when the format is ill in queue, can not decode, it will be null
     */
    private final T payload;
    private final String reference;
    private final String originContent;
    private final String messageId;


    public MessageWrapper(String messageId, @Nullable T payload, String reference, String originContent) {
        this.messageId = messageId;
        this.payload = payload;
        this.reference = reference;
        this.originContent = originContent;
    }
}
