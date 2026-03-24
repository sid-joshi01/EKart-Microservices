package com.ekart.inventroy.mapper;

import com.ekart.inventroy.dto.InventoryRequest;
import com.ekart.inventroy.dto.InventoryResponse;
import com.ekart.inventroy.entity.Inventory;
import org.springframework.stereotype.Component;

@Component
public class InventoryMapper {

    public Inventory toEntity(InventoryRequest request) {
        Inventory inventory = new Inventory();
        inventory.setProductId(request.getProductId());
        inventory.setQuantity(request.getQuantity());
        return inventory;
    }

    public InventoryResponse toResponse(Inventory inventory) {
        InventoryResponse inventoryResponse = new InventoryResponse();
        inventoryResponse.setProductId(inventory.getProductId());
        inventoryResponse.setAvailableQuantity(inventory.getQuantity());
        inventoryResponse.setInStock(inventory.getQuantity() > 0);
        return inventoryResponse;
    }
}
