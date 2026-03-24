package com.ekart.cart.dto;

import com.ekart.cart.model.CartItems;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record CartDto(Long userId, List<CartItems> items, BigDecimal totalAmount, Instant updatedAt) {
}
