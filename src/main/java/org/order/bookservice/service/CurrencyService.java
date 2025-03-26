package org.order.bookservice.service;

import lombok.RequiredArgsConstructor;
import org.order.bookservice.model.CurrencyRate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CurrencyService {
    @Value("${api.nbrb.rates}")
    private String apiUrl;
    private final RestTemplate restTemplate = new RestTemplate();

    @Cacheable("currencyRates")
    public Map<String, BigDecimal> getExchangeRates() {
        CurrencyRate[] rates = restTemplate.getForObject(apiUrl, CurrencyRate[].class);
        Map<String, BigDecimal> exchangeRates = new HashMap<>();

        if (rates != null) {
            for (CurrencyRate rate : rates) {
                BigDecimal officialRate = rate.getCur_OfficialRate();
                BigDecimal scale = BigDecimal.valueOf(rate.getCur_Scale());
                exchangeRates.put(rate.getCur_Abbreviation(), officialRate.divide(scale, 10, RoundingMode.HALF_UP));
            }
        }

        exchangeRates.put("BYN", BigDecimal.ONE);
        return exchangeRates;
    }
}
