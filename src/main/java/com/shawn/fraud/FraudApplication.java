package com.shawn.fraud;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Hello world!
 */
@SpringBootApplication
public class FraudApplication {
    private static final Logger logger = LoggerFactory.getLogger(FraudApplication.class);

    public static void main(String[] args) {
        logger.info("begin to run the application");
        SpringApplication.run(FraudApplication.class, args);
    }
}
