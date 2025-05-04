package com.shawn.fraud.infrastructure;

import com.shawn.fraud.domain.config.AliYunMnsProperties;
import com.shawn.fraud.domain.config.AliYunProperties;
import com.shawn.fraud.domain.config.FraudDetectProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(value = {
        AliYunProperties.class,
        AliYunMnsProperties.class,
        FraudDetectProperties.class
})
public class BaseConfiguration {

}
