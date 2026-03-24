package com.ekart.inventroy.controller;

import com.ekart.inventroy.dto.InventoryRequest;
import com.ekart.inventroy.dto.InventoryResponse;
import com.ekart.inventroy.entity.Inventory;
import com.ekart.inventroy.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public boolean isInStock(@RequestParam String productId, @RequestParam Integer quantity){
        return inventoryService.isInStock(productId, quantity);
    }

//    @PostMapping("/commit")
//    public ResponseEntity<Void> commitInventory(@RequestBody InventoryRequest inventoryRequest){
//        inventoryService.commitInventory(inventoryRequest);
//        return ResponseEntity.ok().build();
//    }

    @PostMapping("/rollback")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<Void> rollbackInventory(@RequestBody InventoryRequest inventoryRequest){
        inventoryService.rollbackInventory(inventoryRequest);
        return ResponseEntity.ok().build();
    }





    // ADMIN
    @GetMapping("/admin/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InventoryResponse> checkInventory(@PathVariable String productId) {
        return ResponseEntity.ok(inventoryService.checkInventory(productId));
    }

    // ADMIN
    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String,String>> addInventory(@RequestBody InventoryRequest inventoryRequest) {
        inventoryService.addInventory(inventoryRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("response", "Inventory added successfully"));
    }

    // ADMIN
    @PatchMapping("/admin")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public void updateInventory(@RequestBody InventoryRequest inventoryRequest){
         inventoryService.updateInventoryByProductId(inventoryRequest);
    }
}
