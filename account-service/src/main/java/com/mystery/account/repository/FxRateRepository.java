package com.mystery.account.repository;

import com.mystery.account.model.FxRate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FxRateRepository extends JpaRepository<FxRate, Long> {
    Optional<FxRate> findTopByBaseCurrencyAndTargetCurrencyOrderByTimestampDesc(String baseCurrency, String targetCurrency);
}

