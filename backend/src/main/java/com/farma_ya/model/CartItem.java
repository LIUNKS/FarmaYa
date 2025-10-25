package com.farma_ya.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "itemcarrito")
public class CartItem {
    public CartItem() {
    }

    public CartItem(Long id, Cart cart, Product product, int quantity, java.time.LocalDateTime addedAt) {
        this.id = id;
        this.cart = cart;
        this.product = product;
        this.quantity = quantity;
        this.addedAt = addedAt;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_carrito_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carrito_id")
    @JsonBackReference
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private Product product;

    @Column(name = "cantidad", nullable = false)
    private int quantity;

    @Column(name = "creado_en")
    private LocalDateTime addedAt = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public java.time.LocalDateTime getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(java.time.LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }
}