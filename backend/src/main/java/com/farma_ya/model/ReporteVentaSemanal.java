package com.farma_ya.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ReporteVentaSemanal")
public class ReporteVentaSemanal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reporte_id")
    private Long reporteId;

    @Column(name = "semana_inicio", nullable = false)
    private LocalDate semanaInicio;

    @Column(name = "semana_fin", nullable = false)
    private LocalDate semanaFin;

    @Column(name = "year_semana", nullable = false, unique = true)
    private String yearSemana; // Formato: "2025-W01"

    @Column(name = "total_pedidos")
    private Integer totalPedidos = 0;

    @Column(name = "total_productos_vendidos")
    private Integer totalProductosVendidos = 0;

    @Column(name = "total_ingresos", precision = 12, scale = 2)
    private BigDecimal totalIngresos = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_mas_vendido_id")
    private Product productoMasVendido;

    @Column(name = "categoria_mas_vendida")
    private String categoriaMasVendida;

    @Column(name = "generado_en")
    private LocalDateTime generadoEn = LocalDateTime.now();

    @OneToMany(mappedBy = "reporte", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DetalleVentaSemanalProducto> detallesProductos;

    // Constructores
    public ReporteVentaSemanal() {
    }

    public ReporteVentaSemanal(LocalDate semanaInicio, LocalDate semanaFin, String yearSemana) {
        this.semanaInicio = semanaInicio;
        this.semanaFin = semanaFin;
        this.yearSemana = yearSemana;
    }

    // Getters y Setters
    public Long getReporteId() {
        return reporteId;
    }

    public void setReporteId(Long reporteId) {
        this.reporteId = reporteId;
    }

    public LocalDate getSemanaInicio() {
        return semanaInicio;
    }

    public void setSemanaInicio(LocalDate semanaInicio) {
        this.semanaInicio = semanaInicio;
    }

    public LocalDate getSemanaFin() {
        return semanaFin;
    }

    public void setSemanaFin(LocalDate semanaFin) {
        this.semanaFin = semanaFin;
    }

    public String getYearSemana() {
        return yearSemana;
    }

    public void setYearSemana(String yearSemana) {
        this.yearSemana = yearSemana;
    }

    public Integer getTotalPedidos() {
        return totalPedidos;
    }

    public void setTotalPedidos(Integer totalPedidos) {
        this.totalPedidos = totalPedidos;
    }

    public Integer getTotalProductosVendidos() {
        return totalProductosVendidos;
    }

    public void setTotalProductosVendidos(Integer totalProductosVendidos) {
        this.totalProductosVendidos = totalProductosVendidos;
    }

    public BigDecimal getTotalIngresos() {
        return totalIngresos;
    }

    public void setTotalIngresos(BigDecimal totalIngresos) {
        this.totalIngresos = totalIngresos;
    }

    public Product getProductoMasVendido() {
        return productoMasVendido;
    }

    public void setProductoMasVendido(Product productoMasVendido) {
        this.productoMasVendido = productoMasVendido;
    }

    public String getCategoriaMasVendida() {
        return categoriaMasVendida;
    }

    public void setCategoriaMasVendida(String categoriaMasVendida) {
        this.categoriaMasVendida = categoriaMasVendida;
    }

    public LocalDateTime getGeneradoEn() {
        return generadoEn;
    }

    public void setGeneradoEn(LocalDateTime generadoEn) {
        this.generadoEn = generadoEn;
    }

    public List<DetalleVentaSemanalProducto> getDetallesProductos() {
        return detallesProductos;
    }

    public void setDetallesProductos(List<DetalleVentaSemanalProducto> detallesProductos) {
        this.detallesProductos = detallesProductos;
    }
}