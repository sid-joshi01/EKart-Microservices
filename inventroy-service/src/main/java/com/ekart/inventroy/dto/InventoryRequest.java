package com.ekart.inventroy.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class InventoryRequest {

    @NotBlank(message = "Product ID must not be blank")
    private String productId;

    @Min(value = 0, message = "Quantity must be 0 or greater")
    private Integer quantity;
}
