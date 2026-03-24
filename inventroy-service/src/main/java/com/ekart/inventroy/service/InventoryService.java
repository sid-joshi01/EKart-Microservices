package com.ekart.inventroy.service;

import com.ekart.inventroy.dto.InventoryRequest;
import com.ekart.inventroy.dto.InventoryResponse;
import com.ekart.inventroy.entity.Inventory;
import com.ekart.inventroy.entity.ProcessedOrder;
import com.ekart.inventroy.event.OrderPlacedEvent;
import com.ekart.inventroy.exception.InventoryNotFountException;
import com.ekart.inventroy.mapper.InventoryMapper;
import com.ekart.inventroy.repository.InventoryRepository;
import com.ekart.inventroy.repository.ProcessedOrderRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;

@Slf4j
@Service
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private InventoryMapper inventoryMapper;

    @Autowired
    private ProcessedOrderRepository processedOrderRepository;

    public InventoryResponse checkInventory(String productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new InventoryNotFountException(productId));
        return inventoryMapper.toResponse(inventory);
    }

    // ADMIN
    public void addInventory(InventoryRequest inventoryRequest) {
        Inventory inventory = inventoryMapper.toEntity(inventoryRequest);
        inventoryRepository.save(inventory);
    }

    public void updateInventoryByProductId(InventoryRequest inventoryRequest) {
        Inventory inventory = inventoryRepository.findByProductId(inventoryRequest.getProductId())
                .orElseThrow(() -> new InventoryNotFountException(inventoryRequest.getProductId()));

        Integer availableQty = inventory.getQuantity();
        Integer orderOty = inventoryRequest.getQuantity();

        if(availableQty < orderOty) {
            throw new RuntimeException("Insufficient Stock for Product ID: " + inventoryRequest.getProductId());
        }
        inventory.setQuantity(availableQty-orderOty);
        inventoryRepository.save(inventory);


    }

    public boolean isInStock(String productId, Integer quantity){
        return inventoryRepository.existsByProductIdAndQuantityIsGreaterThanEqual(productId,quantity);
    }

    @Transactional
    public void updateStock(OrderPlacedEvent event) {
        if(processedOrderRepository.existsByOrderId(event.getOrderId())) {
            return;
        }

        // updating inventory of each product from order
        event.getProducts().forEach(this::updateInventoryByProductId);


        // For Idempotency
        ProcessedOrder processedOrder = new ProcessedOrder();
        processedOrder.setOrderId(event.getOrderId());
        processedOrder.setProcessedAt(LocalDateTime.now());
        processedOrderRepository.save(processedOrder);
    }


//    public void commitInventory(InventoryRequest inventoryRequest) {
//        Inventory inventory = inventoryRepository.findByProductId(inventoryRequest.getProductId())
//                .orElseThrow(() -> new InventoryNotFountException(inventoryRequest.getProductId()));
//
//        Integer availableQty = inventory.getQuantity();
//        Integer orderOty = inventoryRequest.getQuantity();
//
//        if(availableQty < orderOty) {
//            throw new RuntimeException("Insufficient Stock for Product ID: " + inventoryRequest.getProductId());
//        }
//        inventory.setQuantity(availableQty-orderOty);
//        inventoryRepository.save(inventory);
//    }


    public void rollbackInventory(InventoryRequest inventoryRequest) {
        Inventory inventory = inventoryRepository.findByProductId(inventoryRequest.getProductId())
                .orElseThrow(() -> new InventoryNotFountException(inventoryRequest.getProductId()));

        Integer availableQty = inventory.getQuantity();
        Integer orderOty = inventoryRequest.getQuantity();

        if(orderOty != null) {
            inventory.setQuantity(availableQty + orderOty);
            inventoryRepository.save(inventory);
        }
    }
}
