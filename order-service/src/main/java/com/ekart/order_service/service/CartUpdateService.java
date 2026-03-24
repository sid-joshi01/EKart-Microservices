package com.ekart.order_service.service;

import com.ekart.order_service.clients.CartClient;
import com.ekart.order_service.clients.InventoryClient;
import com.ekart.order_service.entity.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CartUpdateService {

    @Autowired
    private CartClient cartClient;

    @Async("asyncTaskExecutor")
    public void clearCartAsync(Long customerId) {
        try {
            log.info("Background thread {} Clearing cart for order {}", Thread.currentThread().getName(), customerId);
            cartClient.clearCart(customerId);
        }
        catch (Exception e) {
            log.error("Failed to clear the cart {} in background. Manual intervention/Retry needed.", customerId, e);
            // Here you could add a retry mechanism or push to a Dead Letter Queue
        }

    }


}
