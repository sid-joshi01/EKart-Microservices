package com.ekart.inventroy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "processed_order", uniqueConstraints = @UniqueConstraint(columnNames = "orderId"))
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessedOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @Column(nullable = false, unique = true)
    private String orderId;
    private LocalDateTime processedAt;
}
