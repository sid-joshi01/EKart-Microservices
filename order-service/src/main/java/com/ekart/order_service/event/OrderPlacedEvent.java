package com.ekart.order_service.event;

import com.ekart.order_service.dto.ProductRequest;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderPlacedEvent {
    private String eventId;
    private String orderId;
    private List<ProductRequest> products;
    private LocalDateTime eventTime;
}
