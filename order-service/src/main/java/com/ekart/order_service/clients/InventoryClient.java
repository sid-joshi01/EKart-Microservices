package com.ekart.order_service.clients;

import com.ekart.order_service.config.interceptor.FeignClientInterceptor;
import com.ekart.order_service.dto.InventoryRequest;
import com.ekart.order_service.dto.InventoryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "inventory-service", configuration = FeignClientInterceptor.class) // FeignClientInterceptor adds Authorization header to outgoing requests from order-service
public interface InventoryClient {

    @GetMapping("/api/inventory")
    boolean isInStock(@RequestParam("productId") String productId, @RequestParam("quantity") Integer quantity);

//    @GetMapping("/api/inventory/{productId}")
//    InventoryResponse checkInventory(@PathVariable("productId") String productId);


    // You don't need commitInventory it is done by kafka in background
//    @PostMapping("/api/inventory/commit")
//    void commitInventory(@RequestBody InventoryRequest inventoryRequest);

    @PostMapping("/api/inventory/rollback")
    void rollbackInventory(@RequestBody InventoryRequest inventoryRequest);

}
