package com.ekart.inventroy.consumer;

import com.ekart.inventroy.event.OrderPlacedEvent;
import com.ekart.inventroy.service.InventoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrderEventConsumer {

    @Autowired
    private InventoryService inventoryService;

    @KafkaListener(topics = "order_event", groupId = "inventory-group")
    public void consume(OrderPlacedEvent event) {
        inventoryService.updateStock(event);
        log.info("Received event: {}", event);
    }

}
