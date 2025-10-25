package com.farma_ya.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import java.time.LocalDateTime;

@Entity
@Table(name = "Direccion")
public class Direccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "direccion_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    @JsonBackReference
    private User user;

    @Column(name = "direccion_linea", length = 255, nullable = false)
    private String direccionLinea;

    @Column(name = "distrito", length = 100)
    private String distrito;

    @Column(name = "ciudad", length = 100, columnDefinition = "VARCHAR(100) DEFAULT 'Lima'")
    private String ciudad = "Lima";

    @Column(name = "referencia", columnDefinition = "TEXT")
    private String referencia;

    @Column(name = "creado_en")
    private LocalDateTime creadoEn = LocalDateTime.now();

    // Constructors
    public Direccion() {
    }

    public Direccion(User user, String direccionLinea, String distrito, String referencia) {
        this.user = user;
        this.direccionLinea = direccionLinea;
        this.distrito = distrito;
        this.referencia = referencia;
    }

    // Getters and Setters
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

    public String getDireccionLinea() {
        return direccionLinea;
    }

    public void setDireccionLinea(String direccionLinea) {
        this.direccionLinea = direccionLinea;
    }

    public String getDistrito() {
        return distrito;
    }

    public void setDistrito(String distrito) {
        this.distrito = distrito;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }
}