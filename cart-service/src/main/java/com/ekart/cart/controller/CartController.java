package com.ekart.cart.controller;

import com.ekart.cart.dto.CartDto;
import com.ekart.cart.model.CartItems;
import com.ekart.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
//@CrossOrigin(origins = "http://localhost:8083")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/{userId}")
    public CartDto getCart(@PathVariable Long userId){
        return cartService.getCart(userId);
    }

    @PostMapping("/{userId}") // At a time single item will get added to cart
    public CartDto addToCart(@PathVariable Long userId, @RequestBody CartItems cartItems){
        return cartService.addToCart(userId, cartItems);
    }

    @DeleteMapping("/{userId}")
    public CartDto removeFromCart(@PathVariable Long userId, @RequestBody CartItems cartItems){
        return cartService.removeProductFromCart(userId, cartItems);
    }

    @DeleteMapping("/clear/{userId}")
    public void clearCart(@PathVariable Long userId){
        cartService.clearCart(userId);
    }

}
