package org.order.bookservice.dto;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        BigDecimal price,
        String currency,
        String category,
        String genre
) {
}
