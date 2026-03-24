package com.ekart.order_service.repository;

import com.ekart.order_service.entity.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order,Long> {

    @EntityGraph(attributePaths = "items") // Optional: fetch items immediately
    Optional<Order> findByOrderId(String id);

    List<Order> findAllByCustomerId(Long customerId);


    @Query("SELECT o.totalAmount FROM Order o WHERE o.orderId = :orderId")
    Optional<BigDecimal> findTotalAmountByOrderId(String orderId);


}
