package com.shawn.fraud.domain;

import java.util.List;

public interface SimpleMessageTemplate<T> {
    public static final String MESSAGE_TEMPLATE_REQUEST="request_message_template";
    public static final String MESSAGE_TEMPLATE_RESPONSE="response_message_template";
    public static final String MESSAGE_TEMPLATE_DLT="dlt_message_template";
    public void send(String requestId, T message);

    public void delete(T message);

    public List<T> list(int maxSize);

    public List<T> list();
}
