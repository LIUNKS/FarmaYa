package com.farma_ya.repository;

import com.farma_ya.model.DetalleVentaSemanalProducto;
import com.farma_ya.model.ReporteVentaSemanal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleVentaSemanalProductoRepository extends JpaRepository<DetalleVentaSemanalProducto, Long> {

    // Buscar detalles por reporte
    List<DetalleVentaSemanalProducto> findByReporte(ReporteVentaSemanal reporte);

    // Buscar detalles por reporte ordenados por cantidad vendida (descendente)
    @Query("SELECT d FROM DetalleVentaSemanalProducto d WHERE d.reporte = :reporte ORDER BY d.cantidadVendida DESC")
    List<DetalleVentaSemanalProducto> findByReporteOrderByCantidadVendidaDesc(
            @Param("reporte") ReporteVentaSemanal reporte);

    // Buscar detalles por reporte ordenados por ingresos (descendente)
    @Query("SELECT d FROM DetalleVentaSemanalProducto d WHERE d.reporte = :reporte ORDER BY d.totalIngresos DESC")
    List<DetalleVentaSemanalProducto> findByReporteOrderByTotalIngresosDesc(
            @Param("reporte") ReporteVentaSemanal reporte);

    // Top productos más vendidos en una semana
    @Query("SELECT d FROM DetalleVentaSemanalProducto d WHERE d.reporte = :reporte ORDER BY d.cantidadVendida DESC LIMIT :limit")
    List<DetalleVentaSemanalProducto> findTopProductosMasVendidos(@Param("reporte") ReporteVentaSemanal reporte,
            @Param("limit") int limit);
}