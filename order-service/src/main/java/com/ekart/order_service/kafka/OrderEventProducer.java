package com.ekart.order_service.kafka;

import com.ekart.order_service.event.OrderPlacedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrderEventProducer {


    public static final String TOPIC = "order_event";

    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    public OrderEventProducer(KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendOrder(OrderPlacedEvent event) {
        // Topic: "order_event", Key: OrderId, Value: The event object
        kafkaTemplate.send(TOPIC, event.getOrderId(), event);
        log.info("Sent message: {}", event);
    }

//    @Autowired
//    private KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

//    public void sendOrder(OrderPlacedEvent event) {
//        kafkaTemplate.send("order_event", event.getOrderId(), event);
//    }
}
