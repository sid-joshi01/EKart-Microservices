package com.ekart.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlaceOrderResponse {
    private boolean orderPlaced;
    List<String> outOfStockProducts;
    private String orderId;
    private BigDecimal totalAmount;
}
