package com.ekart.order_service.clients;

import com.ekart.order_service.config.interceptor.FeignClientInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "cart-service", configuration = FeignClientInterceptor.class)
public interface CartClient {

    @DeleteMapping("/api/cart/clear/{userId}")
    void clearCart(@PathVariable("userId") Long userId);
}
