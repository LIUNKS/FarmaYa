package com.farma_ya.controller;

import com.farma_ya.model.Cart;
import com.farma_ya.model.User;
import com.farma_ya.service.CartService;
import com.farma_ya.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<Cart> getUserCart(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        Cart cart = cartService.getCartByUser(currentUser);
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/add")
    public ResponseEntity<Cart> addProductToCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") int quantity) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        Cart updatedCart = cartService.addToCart(currentUser, productId, quantity);
        return new ResponseEntity<>(updatedCart, HttpStatus.OK);
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<Cart> removeProductFromCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        Cart updatedCart = cartService.removeFromCart(currentUser, productId);
        return ResponseEntity.ok(updatedCart);
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Cart> clearCart(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        Cart clearedCart = cartService.clearCart(currentUser);
        return ResponseEntity.ok(clearedCart);
    }
}