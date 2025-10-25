package com.farma_ya.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Convert;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Pedido")
public class Order {
    public Order() {
    }

    public Order(Long id, User user, java.util.List<OrderItem> items, BigDecimal totalAmount, OrderStatus status,
            java.time.LocalDateTime createdAt) {
        this.id = id;
        this.user = user;
        this.items = items;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pedido_id")
    private Long id;

    @Column(name = "numero_pedido", unique = true)
    private String numeroPedido;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private User user;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<OrderItem> items = new ArrayList<>();

    @Convert(converter = OrderStatusConverter.class)
    @Column(name = "estado")
    private OrderStatus status = OrderStatus.PENDIENTE;

    @Column(name = "subtotal", precision = 10, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "total", precision = 10, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "creado_en")
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "direccion_entrega_id")
    @JsonIgnore
    private Direccion shippingAddress;

    public double getCalculatedTotalAmount() {
        return items.stream()
                .mapToDouble(item -> item.getPrice().doubleValue() * item.getQuantity())
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

    public java.util.List<OrderItem> getItems() {
        return items;
    }

    public void setItems(java.util.List<OrderItem> items) {
        this.items = items;
    }

    public BigDecimal getTotalAmountValue() {
        return totalAmount;
    }

    public double getTotalAmount() {
        return this.totalAmount != null ? this.totalAmount.doubleValue() : 0.0;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getNumeroPedido() {
        return numeroPedido;
    }

    public void setNumeroPedido(String numeroPedido) {
        this.numeroPedido = numeroPedido;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Direccion getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(Direccion shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    @JsonProperty("shippingAddress")
    public String getShippingAddressLine() {
        return shippingAddress != null ? shippingAddress.getDireccionLinea() : null;
    }

    @JsonProperty("shippingDistrict")
    public String getShippingDistrict() {
        return shippingAddress != null ? shippingAddress.getDistrito() : null;
    }

    @JsonProperty("shippingReference")
    public String getShippingReference() {
        return shippingAddress != null ? shippingAddress.getReferencia() : null;
    }

    public void setShippingAddress(String address, String district, String reference) {
        if (this.shippingAddress == null) {
            this.shippingAddress = new Direccion();
        }
        this.shippingAddress.setDireccionLinea(address);
        this.shippingAddress.setDistrito(district);
        this.shippingAddress.setReferencia(reference);
    }
}
