package com.shawn.fraud.domain.model;

import java.math.BigDecimal;

public class Transaction {
    private String id;
    private BigDecimal amount;

    private int age = 0;
    private String country;

    public String getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getCountry() {
        return country;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
