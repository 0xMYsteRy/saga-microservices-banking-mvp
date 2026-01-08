package com.mystery.account.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@IdClass(CurrencyBalance.CurrencyBalanceId.class)
@Table(name = "account_balances")
public class CurrencyBalance {

    @Id
    @Column(name = "account_id")
    private Long accountId;

    @Id
    @Column(name = "currency", length = 3)
    private String currency;

    @Column(name = "balance", precision = 19, scale = 4)
    private BigDecimal balance;

    @Column(name = "last_updated")
    private OffsetDateTime lastUpdated;

    public static class CurrencyBalanceId implements Serializable {
        private Long accountId;
        private String currency;

        public CurrencyBalanceId() {}

        public CurrencyBalanceId(Long accountId, String currency) {
            this.accountId = accountId;
            this.currency = currency;
        }

        // equals/hashCode omitted for brevity; JPA providers may require them
    }
}

