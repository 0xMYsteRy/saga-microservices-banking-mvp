package com.mystery.account.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "fx_rates")
public class FxRate {

    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "base_currency", length = 3)
    private String baseCurrency;

    @Column(name = "target_currency", length = 3)
    private String targetCurrency;

    @Column(name = "rate", precision = 19, scale = 8)
    private BigDecimal rate;

    @Column(name = "timestamp")
    private OffsetDateTime timestamp;

    @Column(name = "source")
    private String source;
}

