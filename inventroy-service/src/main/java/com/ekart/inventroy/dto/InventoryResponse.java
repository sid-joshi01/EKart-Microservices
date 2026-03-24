package com.ekart.inventroy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryResponse {
    private String productId;
    private Integer availableQuantity;
    private boolean inStock;
}
