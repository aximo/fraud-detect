package com.shawn.fraud.infrastructure;

import com.aliyun.mns.client.CloudAccount;
import com.aliyun.mns.client.CloudQueue;
import com.aliyun.mns.client.MNSClient;
import com.aliyun.mns.model.Message;
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


    @Bean(SimpleMessageTemplate.MESSAGE_TEMPLATE_REQUEST)
    public SimpleMessageTemplate<Message> requestMessageTemplate(MNSClient client,
                                                                 FraudDetectProperties fraudDetectProperties
    ) {
        CloudQueue queue = client.getQueueRef(fraudDetectProperties.getRequestQueue());
        return new TransactionSimpleMessageTemplate(queue, fraudDetectProperties);
    }

    @Bean(SimpleMessageTemplate.MESSAGE_TEMPLATE_RESPONSE)
    public SimpleMessageTemplate<Message> responseMessageTemplate(MNSClient client,
                                                                  FraudDetectProperties fraudDetectProperties
    ) {
        CloudQueue queue = client.getQueueRef(fraudDetectProperties.getResponseQueue());
        return new TransactionSimpleMessageTemplate(queue, fraudDetectProperties);
    }

    @Bean(SimpleMessageTemplate.MESSAGE_TEMPLATE_DLT)
    public SimpleMessageTemplate<Message> dltMessageTemplate(MNSClient client,
                                                             FraudDetectProperties fraudDetectProperties
    ) {
        CloudQueue queue = client.getQueueRef(fraudDetectProperties.getDeadQueue());
        return new TransactionSimpleMessageTemplate(queue, fraudDetectProperties);
    }
}
