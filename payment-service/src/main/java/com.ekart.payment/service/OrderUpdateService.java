package com.ekart.payment.service;

import com.ekart.payment.client.OrderClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrderUpdateService {

    @Autowired
    private OrderClient orderClient;

    @Async("asyncTaskExecutor") // Uses the thread pool we discussed
    public void confirmOrderAsync(String orderId) {
        try {
            log.info("Background thread {} confirming order {}", Thread.currentThread().getName(), orderId);
            orderClient.confirmOrder(orderId);
        } catch (Exception e) {
            log.error("Failed to confirm order {} in background. Manual intervention/Retry needed.", orderId, e);
            // Here you could add a retry mechanism or push to a Dead Letter Queue
        }
    }

    @Async("asyncTaskExecutor") // Uses the thread pool we discussed
    public void failOrderAsync(String orderId) {
        try {
            log.info("Background thread {} failed order {}", Thread.currentThread().getName(), orderId);
            orderClient.failOrder(orderId);
        } catch (Exception e) {
            log.error("Failed to fail the order {} in background. Manual intervention/Retry needed.", orderId, e);
            // Here you could add a retry mechanism or push to a Dead Letter Queue
        }
    }
}
