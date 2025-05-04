package com.shawn.fraud.domain.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "ali")
public class AliYunProperties {
    private String key;
    private String secret;
}
