package com.farma_ya.controller;

import com.farma_ya.model.ReporteVentaSemanal;
import com.farma_ya.service.ReporteVentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/reportes")
@CrossOrigin(origins = "*")
public class ReporteVentaController {

    @Autowired
    private ReporteVentaService reporteService;

    /**
     * Generar reporte de una semana específica
     */
    @PostMapping("/generar-semanal")
    public ResponseEntity<Map<String, Object>> generarReporteSemanal(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        Map<String, Object> response = new HashMap<>();

        try {
            ReporteVentaSemanal reporte = reporteService.generarReporteSemanal(fechaInicio, fechaFin);

            response.put("status", "SUCCESS");
            response.put("message", "Reporte generado exitosamente");
            response.put("reporte", reporte);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Error al generar reporte: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Generar reportes automáticos para las últimas N semanas
     */
    @PostMapping("/generar-automaticos/{numeroSemanas}")
    public ResponseEntity<Map<String, Object>> generarReportesAutomaticos(
            @PathVariable int numeroSemanas) {

        Map<String, Object> response = new HashMap<>();

        try {
            List<ReporteVentaSemanal> reportes = reporteService.generarReportesAutomaticos(numeroSemanas);

            response.put("status", "SUCCESS");
            response.put("message", String.format("Se generaron %d reportes", reportes.size()));
            response.put("reportes", reportes);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Error al generar reportes automáticos: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Obtener reportes de un año específico
     */
    @GetMapping("/por-año/{año}")
    public ResponseEntity<Map<String, Object>> getReportesPorAno(@PathVariable int año) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<ReporteVentaSemanal> reportes = reporteService.getReportesPorAno(año);

            response.put("status", "SUCCESS");
            response.put("año", año);
            response.put("total_reportes", reportes.size());
            response.put("reportes", reportes);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Error al obtener reportes: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Obtener los últimos N reportes
     */
    @GetMapping("/ultimos/{limite}")
    public ResponseEntity<Map<String, Object>> getUltimosReportes(@PathVariable int limite) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<ReporteVentaSemanal> reportes = reporteService.getUltimosReportes(limite);

            response.put("status", "SUCCESS");
            response.put("limite", limite);
            response.put("reportes_encontrados", reportes.size());
            response.put("reportes", reportes);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Error al obtener últimos reportes: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Endpoint de información sobre reportes disponibles
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getInfoReportes() {
        Map<String, Object> response = new HashMap<>();

        response.put("status", "SUCCESS");
        response.put("descripcion", "API para generar y consultar reportes de ventas semanales");
        response.put("endpoints", Map.of(
                "POST /api/reportes/generar-semanal", "Generar reporte para una semana específica",
                "POST /api/reportes/generar-automaticos/{n}", "Generar reportes automáticos para las últimas N semanas",
                "GET /api/reportes/por-año/{año}", "Obtener reportes de un año específico",
                "GET /api/reportes/ultimos/{limite}", "Obtener los últimos N reportes"));

        return ResponseEntity.ok(response);
    }
}