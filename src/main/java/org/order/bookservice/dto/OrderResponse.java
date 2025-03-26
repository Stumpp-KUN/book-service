package org.order.bookservice.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrderResponse(
        Long orderId,
        String currency,
        BigDecimal totalPrice
) {
}
