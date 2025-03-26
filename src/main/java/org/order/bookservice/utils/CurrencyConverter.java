package org.order.bookservice.utils;

import lombok.RequiredArgsConstructor;
import org.order.bookservice.service.CurrencyService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CurrencyConverter {
    private final CurrencyService currencyService;

    public BigDecimal convert(BigDecimal amount, String from, String to) {
        Map<String, BigDecimal> rates = currencyService.getExchangeRates();

        if (!rates.containsKey(from) || !rates.containsKey(to)) {
            throw new IllegalArgumentException("Неизвестная валюта: " + from + " или " + to);
        }

        if (from.equals(to)) {
            return amount.setScale(2, RoundingMode.HALF_UP);
        }

        var rateFrom = rates.get(from);
        var rateTo = rates.get(to);

        var amountInBYN = amount.multiply(rateFrom);

        var result = amountInBYN.divide(rateTo, 10, RoundingMode.HALF_UP);

        return result.setScale(2, RoundingMode.HALF_UP);
    }


}

