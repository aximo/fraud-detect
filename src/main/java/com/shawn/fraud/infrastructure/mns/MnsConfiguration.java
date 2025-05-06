package com.shawn.fraud.infrastructure.mns;

import com.aliyun.mns.client.CloudAccount;
import com.aliyun.mns.client.MNSClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shawn.fraud.domain.SimpleMessageTemplate;
import com.shawn.fraud.domain.config.AliYunMnsProperties;
import com.shawn.fraud.domain.config.AliYunProperties;
import com.shawn.fraud.domain.config.FraudDetectProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MnsConfiguration {

    @Bean
    public MNSClient mnsClient(AliYunProperties aliYunProperties, AliYunMnsProperties aliYunMnsProperties) {
        CloudAccount cloudAccount = new CloudAccount(aliYunProperties.getKey(), aliYunProperties.getSecret(), aliYunMnsProperties.getAddress());
        return cloudAccount.getMNSClient();
    }

    @Bean
    public SimpleMessageTemplate messageTemplate(MNSClient client,
                                                 FraudDetectProperties fraudDetectProperties,
                                                 ObjectMapper objectMapper
    ) {
        return new MnsSimpleMessageTemplate(new DefaultQueueProvider(client), fraudDetectProperties, objectMapper);
    }

}
