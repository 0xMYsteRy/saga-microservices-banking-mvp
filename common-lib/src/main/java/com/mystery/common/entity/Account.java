package com.mystery.common.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "accounts",
    indexes = {
        @Index(name = "idx_account_number", columnList = "accountNumber", unique = true),
        @Index(name = "idx_user_id", columnList = "userId"),
        @Index(name = "idx_user_name", columnList = "userName")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @Version
    private Long version;
    
    @Column(unique = true)
    private String accountNumber;
    private String accountType;
    private String userId;
    private String userName;
    private double balance;
    private String status;
    private LocalDateTime createdTimestamp;
}
