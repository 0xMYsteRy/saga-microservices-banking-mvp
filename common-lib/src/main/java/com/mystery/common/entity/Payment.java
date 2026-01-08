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
@Table(name = "payments",
    indexes = {
        @Index(name = "idx_created_by", columnList = "createdBy"),
        @Index(name = "idx_source_account", columnList = "sourceAccountNumber"),
        @Index(name = "idx_status", columnList = "status")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @Column(name = "sourceAccountNumber")
    private String sourceAccountNumber;
    
    @Column(name = "destinationAccountNumber")
    private String destinationAccountNumber;
    
    private double amount;
    
    @Column(name = "status")
    private String status;
    
    private String description;
    private LocalDateTime timestamp;
    
    @Column(name = "createdBy")
    private String createdBy;
    
    private String destinationAccountUserName;
    private double sourceAccountBalance;
    private double destinationAccountBalance;
}
