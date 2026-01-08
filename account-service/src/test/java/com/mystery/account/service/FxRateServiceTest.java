package com.mystery.account.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.mystery.account.model.FxRate;
import com.mystery.account.repository.FxRateRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class FxRateServiceTest {

    @Mock
    FxRateRepository fxRateRepository;

    FxRateService fxRateService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        fxRateService = new FxRateService(fxRateRepository);
    }

    @Test
    void returnsOneForSameCurrency() {
        Optional<BigDecimal> rate = fxRateService.getRate("USD", "USD");
        assertThat(rate).isPresent();
        assertThat(rate.get()).isEqualByComparingTo(BigDecimal.ONE);
    }

    @Test
    void returnsRateFromRepo() {
        FxRate fx = new FxRate(1L, "USD", "EUR", new BigDecimal("0.85"), OffsetDateTime.now(), "test");
        when(fxRateRepository.findTopByBaseCurrencyAndTargetCurrencyOrderByTimestampDesc(anyString(), anyString()))
                .thenReturn(Optional.of(fx));

        Optional<BigDecimal> rate = fxRateService.getRate("USD", "EUR");
        assertThat(rate).isPresent();
        assertThat(rate.get()).isEqualByComparingTo(new BigDecimal("0.85"));
    }
}
