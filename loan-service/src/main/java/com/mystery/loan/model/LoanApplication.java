package com.mystery.loan.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_applications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long applicantId;
    private BigDecimal amount;
    private int termMonths;
    private String currency;
    private String status; // PENDING, APPROVED, REJECTED
    private LocalDateTime createdAt;

}

