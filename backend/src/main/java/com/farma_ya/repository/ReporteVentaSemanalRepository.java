package com.farma_ya.repository;

import com.farma_ya.model.ReporteVentaSemanal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReporteVentaSemanalRepository extends JpaRepository<ReporteVentaSemanal, Long> {

    // Buscar reporte por year-semana (ej: "2025-W01")
    Optional<ReporteVentaSemanal> findByYearSemana(String yearSemana);

    // Buscar reportes por rango de fechas
    @Query("SELECT r FROM ReporteVentaSemanal r WHERE r.semanaInicio >= :fechaInicio AND r.semanaFin <= :fechaFin ORDER BY r.semanaInicio DESC")
    List<ReporteVentaSemanal> findByRangoFechas(@Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin);

    // Obtener últimos N reportes
    @Query("SELECT r FROM ReporteVentaSemanal r ORDER BY r.semanaInicio DESC")
    List<ReporteVentaSemanal> findTopReportes(@Param("limit") int limit);

    // Verificar si existe reporte para una semana específica
    boolean existsByYearSemana(String yearSemana);

    // Buscar reportes de un año específico
    @Query("SELECT r FROM ReporteVentaSemanal r WHERE r.yearSemana LIKE :year% ORDER BY r.semanaInicio DESC")
    List<ReporteVentaSemanal> findByYear(@Param("year") String year);
}