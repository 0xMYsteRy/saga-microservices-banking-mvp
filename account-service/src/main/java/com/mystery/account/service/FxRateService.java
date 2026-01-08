package com.mystery.account.service;

import com.mystery.account.model.FxRate;
import com.mystery.account.repository.FxRateRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Tiny FX rate lookup service backed by DB with an in-memory cache.
 */
@Service
@RequiredArgsConstructor
public class FxRateService {

    private final FxRateRepository fxRateRepository;

    // simple in-memory cache: key = BASE:TARGET
    private final Map<String, BigDecimal> cache = new ConcurrentHashMap<>();

    public Optional<BigDecimal> getRate(String base, String target) {
        if (base.equalsIgnoreCase(target)) {
            return Optional.of(BigDecimal.ONE);
        }

        String key = base.toUpperCase() + ":" + target.toUpperCase();
        if (cache.containsKey(key)) {
            return Optional.of(cache.get(key));
        }

        Optional<FxRate> dbRate = fxRateRepository.findTopByBaseCurrencyAndTargetCurrencyOrderByTimestampDesc(base.toUpperCase(), target.toUpperCase());
        if (dbRate.isPresent()) {
            BigDecimal r = dbRate.get().getRate();
            cache.put(key, r);
            return Optional.of(r);
        }

        // Could call external provider here; for now return empty
        return Optional.empty();
    }

    public FxRate saveRate(String base, String target, BigDecimal rate, String source) {
        FxRate fx = new FxRate();
        fx.setBaseCurrency(base.toUpperCase());
        fx.setTargetCurrency(target.toUpperCase());
        fx.setRate(rate);
        fx.setTimestamp(OffsetDateTime.now());
        fx.setSource(source);
        FxRate saved = fxRateRepository.save(fx);
        cache.put(fx.getBaseCurrency() + ":" + fx.getTargetCurrency(), fx.getRate());
        return saved;
    }
}

