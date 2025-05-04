package com.shawn.fraud.domain;

import com.shawn.fraud.domain.model.Transaction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static com.shawn.fraud.domain.FraudError.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class SimplestFraudServiceTest {

    @Test
    void detect_success() {
        SimplestFraudService fraudService = new SimplestFraudService();
        assertDoesNotThrow(() -> {
            fraudService.detect(create(25, 1000, "china"));
        });
    }

    @Test
    void detect_too_young() {
        SimplestFraudService fraudService = new SimplestFraudService();
        try {
            fraudService.detect(create(10, 1000, "china"));
        } catch (FraudException exception) {
            Assertions.assertEquals(TOO_YOUNG, exception.getError());
        } catch (Exception exception) {
            Assertions.fail(exception);
        }
    }

    @Test
    void detect_too_too_big_amount() {
        SimplestFraudService fraudService = new SimplestFraudService();
        try {
            fraudService.detect(create(35, 20000, "china"));
        } catch (FraudException exception) {
            Assertions.assertEquals(TOO_BIG_AMOUNT, exception.getError());
        } catch (Exception exception) {
            Assertions.fail(exception);
        }
    }


    @Test
    void detect_too_big_amount() {
        SimplestFraudService fraudService = new SimplestFraudService();
        try {
            fraudService.detect(create(17, 3000, "china"));
        } catch (FraudException exception) {
            Assertions.assertEquals(TOO_BIG_AMOUNT, exception.getError());
        } catch (Exception exception) {
            Assertions.fail(exception);
        }
    }

    @Test
    void detect_country_not_supported() {
        SimplestFraudService fraudService = new SimplestFraudService();
        try {
            fraudService.detect(create(17, 100, "japan"));
        } catch (FraudException exception) {
            Assertions.assertEquals(COUNTRY_NOT_SUPPORTED, exception.getError());
        } catch (Exception exception) {
            Assertions.fail(exception);
        }
    }

    public Transaction create(int age, long amount, String country) {
        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID().toString());
        transaction.setAge(age);
        transaction.setAmount(BigDecimal.valueOf(amount));
        transaction.setCountry(country);
        return transaction;
    }
}