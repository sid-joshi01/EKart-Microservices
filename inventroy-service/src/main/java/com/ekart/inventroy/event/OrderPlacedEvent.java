package com.ekart.inventroy.event;

import com.ekart.inventroy.dto.InventoryRequest;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderPlacedEvent {
    private String eventId;
    private String orderId;
    private List<InventoryRequest> products;
    private LocalDateTime eventTime;
}
