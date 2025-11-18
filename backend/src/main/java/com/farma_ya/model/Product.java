package com.farma_ya.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Producto")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "producto_id")
    private Integer id;

    @Column(name = "sku", unique = true)
    private String sku;

    @Column(name = "nombre", nullable = false)
    private String name;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String description;

    @Column(name = "presentacion")
    private String presentacion;

    @Column(name = "principio_activo")
    private String principioActivo;

    @Column(name = "precio", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "categoria")
    private String categoria;

    @Column(name = "imagen_url", length = 500)
    private String imageUrl;

    @Column(name = "stock", nullable = false)
    private Integer stock = 0;

    @Column(name = "activo")
    private Boolean activo = true;

    @Column(name = "creado_en")
    private LocalDateTime creadoEn = LocalDateTime.now();

    // Constructores
    public Product() {
    }

    public Product(String sku, String name, String description, BigDecimal price, String categoria) {
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.price = price;
        this.categoria = categoria;
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPresentacion() {
        return presentacion;
    }

    public void setPresentacion(String presentacion) {
        this.presentacion = presentacion;
    }

    public String getPrincipioActivo() {
        return principioActivo;
    }

    public void setPrincipioActivo(String principioActivo) {
        this.principioActivo = principioActivo;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }
}
