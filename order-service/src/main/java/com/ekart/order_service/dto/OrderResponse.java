package com.ekart.order_service.dto;

import com.ekart.order_service.entity.OrderItems;
import enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {
    private String orderId;
    private Long customerId;
    private BigDecimal totalAmount;
    private OrderStatus orderStatus;
    private Instant instant;
    private List<OrderItemsResponse> items = new ArrayList<>();
}
