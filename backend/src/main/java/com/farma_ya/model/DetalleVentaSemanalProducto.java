package com.farma_ya.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "DetalleVentaSemanalProducto")
public class DetalleVentaSemanalProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detalle_id")
    private Long detalleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporte_id", nullable = false)
    private ReporteVentaSemanal reporte;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Product producto;

    @Column(name = "cantidad_vendida", nullable = false)
    private Integer cantidadVendida;

    @Column(name = "total_ingresos", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalIngresos;

    // Constructores
    public DetalleVentaSemanalProducto() {
    }

    public DetalleVentaSemanalProducto(ReporteVentaSemanal reporte, Product producto,
            Integer cantidadVendida, BigDecimal totalIngresos) {
        this.reporte = reporte;
        this.producto = producto;
        this.cantidadVendida = cantidadVendida;
        this.totalIngresos = totalIngresos;
    }

    // Getters y Setters
    public Long getDetalleId() {
        return detalleId;
    }

    public void setDetalleId(Long detalleId) {
        this.detalleId = detalleId;
    }

    public ReporteVentaSemanal getReporte() {
        return reporte;
    }

    public void setReporte(ReporteVentaSemanal reporte) {
        this.reporte = reporte;
    }

    public Product getProducto() {
        return producto;
    }

    public void setProducto(Product producto) {
        this.producto = producto;
    }

    public Integer getCantidadVendida() {
        return cantidadVendida;
    }

    public void setCantidadVendida(Integer cantidadVendida) {
        this.cantidadVendida = cantidadVendida;
    }

    public BigDecimal getTotalIngresos() {
        return totalIngresos;
    }

    public void setTotalIngresos(BigDecimal totalIngresos) {
        this.totalIngresos = totalIngresos;
    }
}