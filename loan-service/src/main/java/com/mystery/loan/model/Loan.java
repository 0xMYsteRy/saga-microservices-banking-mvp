package com.mystery.loan.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loans")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long applicationId;
    private Long borrowerId;
    private BigDecimal principal;
    private BigDecimal outstanding;
    private double interestRate; // annual
    private LocalDateTime disbursedAt;
}

