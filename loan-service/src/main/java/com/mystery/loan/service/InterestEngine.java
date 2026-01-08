package com.mystery.loan.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class InterestEngine {

    // simple interest calc for example: principal * rate * months / 12
    public BigDecimal calculateAccruedInterest(BigDecimal principal, double annualRate, int months) {
        if (principal == null) return BigDecimal.ZERO;
        BigDecimal rate = BigDecimal.valueOf(annualRate).divide(BigDecimal.valueOf(12), 8, RoundingMode.HALF_UP);
        BigDecimal interest = principal.multiply(rate).multiply(BigDecimal.valueOf(months));
        return interest.setScale(2, RoundingMode.HALF_UP);
    }
}

