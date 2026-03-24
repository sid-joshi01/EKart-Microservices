package com.ekart.payment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryRequest {

    @NotNull(message = "Product id is required")
    private String productId;

    @NotNull(message = "Quantity is required")
    private Integer quantity;
}
