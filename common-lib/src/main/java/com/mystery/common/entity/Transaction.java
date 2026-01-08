package com.mystery.common.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "transactions",
    indexes = {
        @Index(name = "idx_username", columnList = "username"),
        @Index(name = "idx_account_number", columnList = "accountNumber"),
        @Index(name = "idx_timestamp", columnList = "timestamp")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @Column(name = "accountNumber")
    private String accountNumber;
    
    private double amount;
    private String type;
    private String description;
    private String status;
    private String reference;
    
    @Column(name = "timestamp")
    private LocalDateTime timestamp;
    
    @Column(name = "username")
    private String username; // Added field to track which user owns this transaction
    
    private double balance; // Renamed from balanceAfterTransaction to balance
}
