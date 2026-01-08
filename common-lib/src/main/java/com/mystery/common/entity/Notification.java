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
@Table(name = "notifications",
    indexes = {
        @Index(name = "idx_user_name", columnList = "userName"),
        @Index(name = "idx_timestamp", columnList = "timestamp")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @Column(name = "userName")
    private String userName;
    
    private String subject;
    private String message;
    
    @Column(name = "timestamp")
    private LocalDateTime timestamp;
}
