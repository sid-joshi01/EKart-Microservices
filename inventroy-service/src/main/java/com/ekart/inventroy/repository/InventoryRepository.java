package com.ekart.inventroy.repository;

import com.ekart.inventroy.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProductId(String productId);

    boolean existsByProductIdAndQuantityIsGreaterThanEqual(String productId, Integer quantity);

}
