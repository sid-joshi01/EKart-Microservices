package com.ekart.order_service.dto;

import java.math.BigDecimal;

public record OrderItemsResponse(String productId, Integer quantity, BigDecimal price) {
}
