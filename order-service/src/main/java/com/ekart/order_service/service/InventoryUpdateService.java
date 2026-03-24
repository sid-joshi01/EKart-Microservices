package com.ekart.order_service.service;

import com.ekart.order_service.clients.InventoryClient;
import com.ekart.order_service.dto.InventoryRequest;
import com.ekart.order_service.dto.ProductRequest;
import com.ekart.order_service.event.OrderPlacedEvent;
import com.ekart.order_service.kafka.OrderEventProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Configuration
@Slf4j
public class InventoryUpdateService {

    @Autowired
    private InventoryClient inventoryClient;


    @Autowired
    private OrderEventProducer orderEventProducer;

    @Async("asyncTaskExecutor")
    public void rollBackInventoryAsync(String productId, Integer quantity) {
        try {
            log.info("Rolling back Inventory with product id {} and quantity {}", productId, quantity);
            inventoryClient.rollbackInventory(new InventoryRequest(productId, quantity));
        }catch (Exception ex){
            log.error("rollBackInventoryAsync Exception: ", ex);
            throw new RuntimeException(ex);
        }
    }


    @Async("asyncTaskExecutor")
    public void produceKafkaEvent(String orderId, List<ProductRequest> products) {
       try {
           log.info("Produced Inventory with order id {} and products {}", orderId, products);
           OrderPlacedEvent event = new OrderPlacedEvent();
           event.setEventId(UUID.randomUUID().toString());
           event.setOrderId(orderId);
           event.setProducts(products);
           event.setEventTime(LocalDateTime.now());
           orderEventProducer.sendOrder(event);
       } catch (Exception e) {
           log.warn("produceKafkaEvent Exception: ", e);
           throw new RuntimeException(e);
       }
    }
}
