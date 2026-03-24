package com.ekart.order_service.entity;


import enums.OrderStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String orderId;
    private Long customerId;
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus orderStatus;

    private Instant instant;

    @OneToMany(
            mappedBy = "order",
            cascade =  CascadeType.ALL,
            orphanRemoval = true
    )
    private List<OrderItems> items = new ArrayList<>();

//    public void addOrderItem(OrderItems item) {
//        items.add(item);
//        item.setOrder(this); // This line updates the database foreign key!
//    }

}
