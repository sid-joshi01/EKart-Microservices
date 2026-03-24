package com.ekart.payment.client;

import com.ekart.payment.interceptor.FeignClientInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@FeignClient(name = "order-service" , url = "${orders.service.url}", configuration =  FeignClientInterceptor.class)
public interface OrderClient {

    @PostMapping("/api/order/place/{orderId}/confirm")
    void confirmOrder(@PathVariable String orderId);

    @PostMapping("/api/order/place/{orderId}/fail")
    void failOrder(@PathVariable String orderId);

    @GetMapping("/api/order/total-amount")
    Map<String,BigDecimal> findTotalAmountByOrderId(@RequestParam("orderId") String orderId);


}
