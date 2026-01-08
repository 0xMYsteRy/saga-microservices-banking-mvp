package com.mystery.loan.service;

import org.springframework.stereotype.Service;

@Service
public class CreditScoringService {

    // placeholder simple scoring: returns score 300-850
    public int scoreApplicant(Long applicantId) {
        // In real world, call credit bureau etc. Here deterministic simple function
        return (int)(300 + (applicantId % 551));
    }

    public boolean isAcceptable(int score, int threshold) {
        return score >= threshold;
    }
}

