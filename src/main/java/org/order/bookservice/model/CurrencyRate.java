package org.order.bookservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyRate {
    @JsonProperty("Cur_Abbreviation")
    private String cur_Abbreviation;

    @JsonProperty("Cur_OfficialRate")
    private BigDecimal cur_OfficialRate;

    @JsonProperty("Cur_Scale")
    private int cur_Scale;
}
