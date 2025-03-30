package org.order.bookservice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.order.bookservice.service.CurrencyService;
import org.order.bookservice.utils.CurrencyConverter;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CurrencyServiceTest {

    @InjectMocks
    private CurrencyConverter currencyConverter;

    @Mock
    private CurrencyService currencyService;

    @Test
    void shouldThrowExceptionWhenCurrencyNotFound() {
        Map<String, BigDecimal> rates = new HashMap<>();
        rates.put("RUB", BigDecimal.valueOf(3.5));
        when(currencyService.getExchangeRates()).thenReturn(rates);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
                currencyConverter.convert(BigDecimal.valueOf(100), "USD", "EUR")
        );

        assertEquals("Неизвестная валюта: USD или EUR", thrown.getMessage());
    }

    @Test
    void shouldHandleExternalApiFail() {
        when(currencyService.getExchangeRates()).thenThrow(new RuntimeException("API недоступен"));

        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
                currencyConverter.convert(BigDecimal.valueOf(100), "USD", "EUR")
        );

        assertEquals("API недоступен", thrown.getMessage());
        verify(currencyService, times(1)).getExchangeRates();
    }
}
