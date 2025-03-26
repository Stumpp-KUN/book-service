package org.order.bookservice.dto;

import java.util.List;

public record ProductRequest (
        List<Long> productIds,
        String currency
) {
}
