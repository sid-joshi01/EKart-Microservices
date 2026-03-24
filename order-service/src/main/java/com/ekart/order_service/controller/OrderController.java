package com.ekart.order_service.controller;

import com.ekart.order_service.dto.OrderResponse;
import com.ekart.order_service.dto.PlaceOrderResponse;
import com.ekart.order_service.dto.ProductRequest;

import com.ekart.order_service.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
//@CrossOrigin(origins = "http://localhost:8086") // To allow your HTML to call the API
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/place/{customerId}")
    @ResponseStatus(HttpStatus.CREATED)
    public PlaceOrderResponse placeOrder(@PathVariable Long customerId, @RequestBody @Valid List<ProductRequest> products){
        return orderService.placeOrder(customerId, products);
    }

    @GetMapping("/{customerId}")
    @ResponseStatus(HttpStatus.OK)
    public List<String> getAllOrders(@PathVariable Long customerId){
        return orderService.getAllOrdersByCustomerId(customerId);
    }

    @GetMapping("/order-details/{orderId}")
    @ResponseStatus(HttpStatus.OK)
    public OrderResponse findOrderById(@PathVariable String orderId){
        return orderService.fetchOrderDetailsById(orderId);
    }


    // access following method through payment service if payment is successfull
    @PostMapping("/place/{orderId}/confirm")
    @ResponseStatus(HttpStatus.OK)
    public void confirmOrder(@PathVariable String orderId){
        orderService.confirmOrder(orderId);
    }

    // access following method through payment service if payment is failed
    @PostMapping("/place/{orderId}/fail")
    @ResponseStatus(HttpStatus.OK)
    public void failOrder(@PathVariable String orderId){
        orderService.failOrder(orderId);
    }

    @GetMapping("/total-amount")
    @ResponseStatus(HttpStatus.OK)
    public Map<String,BigDecimal> findTotalAmountByOrderId(@RequestParam("orderId") String orderId){
        return orderService.findTotalAmount(orderId);
    }
}
