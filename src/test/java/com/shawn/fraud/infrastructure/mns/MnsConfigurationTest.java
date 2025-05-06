package com.shawn.fraud.infrastructure.mns;

import com.aliyun.mns.client.MNSClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shawn.fraud.domain.config.FraudDetectProperties;
import com.shawn.fraud.infrastructure.mns.MnsConfiguration;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class MnsConfigurationTest {

    private final MnsConfiguration mnsConfiguration = new MnsConfiguration();
    private final FraudDetectProperties fraudDetectProperties = new FraudDetectProperties();
    private final MNSClient client = Mockito.mock(MNSClient.class);

    @Test
    void requestMessageTemplate() {
        assertDoesNotThrow(() -> {
            mnsConfiguration.messageTemplate(client, fraudDetectProperties, new ObjectMapper());
        });

    }

}