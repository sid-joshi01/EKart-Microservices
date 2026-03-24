package com.ekart.order_service.service;

import com.ekart.order_service.clients.CartClient;
import com.ekart.order_service.clients.InventoryClient;

import com.ekart.order_service.dto.*;
import com.ekart.order_service.entity.Order;
import com.ekart.order_service.entity.OrderItems;
import com.ekart.order_service.event.OrderPlacedEvent;
import com.ekart.order_service.kafka.OrderEventProducer;
import com.ekart.order_service.repository.OrderRepository;
import enums.OrderStatus;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private InventoryClient inventoryClient;

    @Autowired
    private OrderEventProducer orderEventProducer;

    @Autowired
    private CartUpdateService cartUpdateService;

    @Autowired
    private InventoryUpdateService inventoryUpdateService;


    // fallback method when inventory service is unavailable
    public PlaceOrderResponse orderFallback(Exception ex) {
        return new PlaceOrderResponse(false, null, ex.getMessage(), null);
    }



    @Transactional
    @CircuitBreaker(name = "orderService", fallbackMethod = "orderFallback")
    public PlaceOrderResponse placeOrder(Long customerId, List<ProductRequest> products) {

        log.info("Placing order with product size: " + products.size());
        List<String> outOfStockProducts = products.stream()
                .filter(product -> !inventoryClient.isInStock(product.getProductId(), product.getQuantity())) // if product is not available store it to the list to show unavailable on ui
                .map(ProductRequest::getProductId)
                .toList();

        if(outOfStockProducts.isEmpty()){

            Order order = new Order();
            String orderId = UUID.randomUUID().toString();
            order.setOrderId(orderId);
            order.setTotalAmount(calculateTotalAmount(products));
            order.setCustomerId(customerId);
            order.setInstant(Instant.now());
            order.setOrderStatus(OrderStatus.CREATED);

            // mapping request obj to entity
            List<OrderItems> orderItems = toOrderItemsEntity(products);// converting ProductRequest items to OrderRequest items
            orderItems.forEach(item -> item.setOrder(order)); // setting each item in OrderItems
            order.setItems(orderItems); // saving in db

            orderRepository.save(order);

            // producing event using kafka in background using async
            inventoryUpdateService.produceKafkaEvent(orderId, products);
//            OrderPlacedEvent event = new OrderPlacedEvent();
//            event.setEventId(UUID.randomUUID().toString());
//            event.setOrderId(orderId);
//            event.setProducts(products);
//            event.setEventTime(LocalDateTime.now());
//            orderEventProducer.sendOrder(event);
            return new PlaceOrderResponse(true, List.of(), orderId, order.getTotalAmount());
        }else{
            return new PlaceOrderResponse(false, outOfStockProducts, null, null);
        }
    }

    public BigDecimal calculateTotalAmount(List<ProductRequest> products){
        return products.stream()
                .map(p -> p.getPrice().multiply(BigDecimal.valueOf(p.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    public List<OrderItems> toOrderItemsEntity(List<ProductRequest> products){
        return products.stream().map(x -> new OrderItems(x.getProductId(), x.getPrice(), x.getQuantity()))
                .toList();
    }

    public List<String> getAllOrdersByCustomerId(Long customerId){
        List<Order> orders = orderRepository.findAllByCustomerId(customerId);
        return orders.stream().map(Order::getOrderId)
                .toList();
    }

    // for user
    public OrderResponse fetchOrderDetailsById(String orderId){

        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        OrderResponse orderResponse = new OrderResponse();
        log.info("Fetching order details for orderId with size: " + order.getItems().size());
        if (order.getItems() != null) {
            // Link each item to this order
            order.getItems().forEach(item -> item.setOrder(order));
        }
        List<OrderItemsResponse> items = order.getItems().stream().map(x -> new OrderItemsResponse(x.getProductId(), x.getQuantity(), x.getPrice())).toList();

        orderResponse.setCustomerId(order.getCustomerId());
        orderResponse.setOrderId(order.getOrderId());
        orderResponse.setOrderStatus(order.getOrderStatus());
        orderResponse.setTotalAmount(order.getTotalAmount());
        orderResponse.setItems(items);
        orderResponse.setInstant(order.getInstant());
        return orderResponse;
    }

    @Transactional
    public void confirmOrder(String orderId){
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        order.setOrderStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        cartUpdateService.clearCartAsync(order.getCustomerId());// removing items from cart in background
//        commit inventory done by kafka in background
//        order.getItems().forEach(item -> inventoryClient.commitInventory(new InventoryRequest(item.getProductId(), item.getQuantity())));


    }

    @Transactional
    public void failOrder(String orderId){
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        order.setOrderStatus(OrderStatus.FAILED);
        orderRepository.save(order);

        // rolling back inventory stock
//        order.getItems().forEach(item -> inventoryClient.rollbackInventory(new InventoryRequest(item.getProductId(), item.getQuantity())));

        order.getItems().forEach(item -> inventoryUpdateService.rollBackInventoryAsync(item.getProductId(), item.getQuantity()));

    }

    public Map<String,BigDecimal> findTotalAmount(String orderId){
        BigDecimal amount = orderRepository.findTotalAmountByOrderId(orderId).orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        return Map.of("amount",amount);
    }

}
