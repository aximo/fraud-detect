package com.shawn.fraud.domain.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "ali.mns")
public class AliYunMnsProperties {
    private String address;
}
