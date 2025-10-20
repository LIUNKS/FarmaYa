package com.farma_ya.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Carrito")
public class Cart {
    public Cart() {
    }

    public Cart(Long id, User user, java.util.List<CartItem> items, java.time.LocalDateTime createdAt) {
        this.id = id;
        this.user = user;
        this.items = items;
        this.createdAt = createdAt;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "carrito_id")
    private Long id;

    @OneToOne
    @JoinColumn(name = "usuario_id")
    private User user;

    @Column(name = "session_token")
    private String sessionToken;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    @Column(name = "creado_en")
    private LocalDateTime createdAt = LocalDateTime.now();

    public double getTotalAmount() {
        return items.stream()
                .mapToDouble(item -> item.getProduct().getPrice().doubleValue() * item.getQuantity())
                .sum();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public java.util.List<CartItem> getItems() {
        return items;
    }

    public void setItems(java.util.List<CartItem> items) {
        this.items = items;
    }

    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }
}