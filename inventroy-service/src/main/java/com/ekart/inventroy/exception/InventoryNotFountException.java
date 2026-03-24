package com.ekart.inventroy.exception;

public class InventoryNotFountException extends RuntimeException {

    public InventoryNotFountException(String productId) {
        super("Inventory Not Fount: " + productId);
    }
}
