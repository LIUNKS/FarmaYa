package com.farma_ya.controller;

import com.farma_ya.model.Order;
import com.farma_ya.model.User;
import com.farma_ya.service.OrderService;
import com.farma_ya.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<Order> createOrder(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        Order newOrder = orderService.createOrderFromCart(currentUser);
        return new ResponseEntity<>(newOrder, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Order>> getUserOrders(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        List<Order> orders = orderService.getOrdersByUser(currentUser);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        Order order = orderService.getOrderById(id);
        if (!order.getUser().getId().equals(currentUser.getId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        Order updatedOrder = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(updatedOrder);
    }
}