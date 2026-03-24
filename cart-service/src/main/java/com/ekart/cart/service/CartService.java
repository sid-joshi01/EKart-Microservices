package com.ekart.cart.service;

import com.ekart.cart.dto.CartDto;
import com.ekart.cart.model.Cart;
import com.ekart.cart.model.CartItems;
import com.ekart.cart.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    public CartDto getCart(Long userId){
        Cart mycart = cartRepository.findByUserId(userId).orElseGet(() -> createNewCart(userId));
        return new CartDto(mycart.getUserId(), mycart.getItems(), mycart.getTotalAmount(), mycart.getUpdatedAt());
    }

    // this method is used for internal purpose
    public Cart getCartEntity(Long userId){
        Cart mycart = cartRepository.findByUserId(userId).orElseGet(() -> createNewCart(userId));
        return mycart;
    }

    private Cart createNewCart(Long userId) {
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setTotalAmount(BigDecimal.ZERO);
        cart.setUpdatedAt(Instant.now());
        return cartRepository.save(cart);
    }

    public CartDto addToCart(Long userId, CartItems cartItems){
        Cart cart = getCartEntity(userId); // fetch the user cart

        // fetch the items in the cart of user
        // if product is present update the quantity
        // if not then simply add it to the cart
        cart.getItems().stream()
                .filter(item -> item.getProductId().equals(cartItems.getProductId()))
                .findFirst()
                .ifPresentOrElse(
                        existing -> existing.setQuantity(existing.getQuantity() + cartItems.getQuantity()),
                                () -> cart.getItems().add(cartItems)
                );
        recalculateTotal(cart);
        cart.setUpdatedAt(Instant.now());
        cartRepository.save(cart);
        return new CartDto(cart.getUserId(), cart.getItems(), cart.getTotalAmount(), cart.getUpdatedAt());
    }

    public void recalculateTotal(Cart cart){
         BigDecimal total = cart.getItems().stream()
                .map(p -> p.getPrice().multiply(BigDecimal.valueOf(p.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cart.setTotalAmount(total);
    }

    public CartDto removeProductFromCart(Long userId, CartItems cartItems){
        Cart cart = getCartEntity(userId);

        cart.getItems().removeIf(product -> product.getProductId().equals(cartItems.getProductId()));

        recalculateTotal(cart);
        cart.setUpdatedAt(Instant.now());
        cartRepository.save(cart);
        return new CartDto(cart.getUserId(), cart.getItems(), cart.getTotalAmount(), cart.getUpdatedAt());

    }

    public void clearCart(Long userId) {
        Cart cart = getCartEntity(userId);
        cart.getItems().clear();
        cart.setTotalAmount(BigDecimal.ZERO);
        cartRepository.save(cart);
    }
}
