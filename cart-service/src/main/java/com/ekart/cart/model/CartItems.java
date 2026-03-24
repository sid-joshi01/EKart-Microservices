package com.ekart.cart.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItems {
    private String productId;
    private String productName;
    private BigDecimal price;
    private Integer quantity;
}
