package com.mystery.account.repository;

import com.mystery.account.model.CurrencyBalance;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurrencyBalanceRepository extends JpaRepository<CurrencyBalance, CurrencyBalance.CurrencyBalanceId> {
    List<CurrencyBalance> findByAccountId(Long accountId);
    Optional<CurrencyBalance> findByAccountIdAndCurrency(Long accountId, String currency);
}

