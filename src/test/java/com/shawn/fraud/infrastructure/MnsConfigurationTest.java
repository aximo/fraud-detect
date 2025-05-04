package com.shawn.fraud.infrastructure;

import com.aliyun.mns.client.MNSClient;
import com.aliyun.mns.model.Message;
import com.shawn.fraud.domain.SimpleMessageTemplate;
import com.shawn.fraud.domain.config.FraudDetectProperties;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class MnsConfigurationTest {

    private final MnsConfiguration mnsConfiguration = new MnsConfiguration();
    private final FraudDetectProperties fraudDetectProperties = new FraudDetectProperties();
    private final MNSClient client = Mockito.mock(MNSClient.class);

    @Test
    void requestMessageTemplate() {
        assertDoesNotThrow(() -> {
            mnsConfiguration.requestMessageTemplate(client, fraudDetectProperties);
        });

    }

    @Test
    void responseMessageTemplate() {
        assertDoesNotThrow(() -> {
            mnsConfiguration.responseMessageTemplate(client, fraudDetectProperties);
        });
    }

    @Test
    void dltMessageTemplate() {
        assertDoesNotThrow(() -> {
            mnsConfiguration.dltMessageTemplate(client, fraudDetectProperties);
        });
    }
}