package com.ekart.payment.client;

import com.ekart.payment.interceptor.FeignClientInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "inventory-service" , url = "${inventory.service.url}", configuration =  FeignClientInterceptor.class)
public interface InventoryClient {

    @PostMapping("/api/inventory/{orderId}/commit")
    void commitInventory(@PathVariable String orderId);

    @PostMapping("/api/inventory/{orderId}/rollback")
    void rollbackInventory(@PathVariable String orderId);
}
