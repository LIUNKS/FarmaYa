package com.farma_ya.controller;

import com.farma_ya.model.ReporteVentaSemanal;
import com.farma_ya.service.ExcelExportService;
import com.farma_ya.service.ReporteVentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Reportes", description = "Generación y consulta de reportes de ventas semanales")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/reportes")
@CrossOrigin(origins = "*")
public class ReporteVentaController {

    @Autowired
    private ReporteVentaService reporteService;

    @Autowired
    private ExcelExportService excelExportService;

    @Operation(summary = "Generar reporte semanal", description = "Genera un reporte de ventas para una semana específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reporte generado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Fechas inválidas"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping("/generar-semanal")
    public ResponseEntity<Map<String, Object>> generarReporteSemanal(
            @Parameter(description = "Fecha de inicio de la semana (formato: YYYY-MM-DD)", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @Parameter(description = "Fecha de fin de la semana (formato: YYYY-MM-DD)", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

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

    @Operation(summary = "Generar reportes automáticos", description = "Genera reportes automáticos para las últimas N semanas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reportes generados exitosamente"),
            @ApiResponse(responseCode = "400", description = "Número de semanas inválido"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping("/generar-automaticos/{numeroSemanas}")
    public ResponseEntity<Map<String, Object>> generarReportesAutomaticos(
            @Parameter(description = "Número de semanas hacia atrás para generar reportes", required = true) @PathVariable int numeroSemanas) {

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

    @Operation(summary = "Obtener reportes por año", description = "Obtiene todos los reportes de ventas de un año específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reportes obtenidos exitosamente"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/por-año/{año}")
    public ResponseEntity<Map<String, Object>> getReportesPorAno(
            @Parameter(description = "Año para consultar reportes (ej: 2024)", required = true) @PathVariable int año) {
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

    @Operation(summary = "Obtener últimos reportes", description = "Obtiene los últimos N reportes de ventas ordenados por fecha")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reportes obtenidos exitosamente"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/ultimos/{limite}")
    public ResponseEntity<Map<String, Object>> getUltimosReportes(
            @Parameter(description = "Número máximo de reportes a retornar", required = true) @PathVariable int limite) {
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

    @Operation(summary = "Información de la API", description = "Obtiene información sobre los endpoints disponibles para reportes")
    @ApiResponse(responseCode = "200", description = "Información obtenida exitosamente")
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getInfoReportes() {
        Map<String, Object> response = new HashMap<>();

        response.put("status", "SUCCESS");
        response.put("descripcion", "API para generar y consultar reportes de ventas semanales y diarias");
        response.put("endpoints", Map.of(
                "POST /api/reportes/generar-semanal", "Generar reporte para una semana específica",
                "POST /api/reportes/generar-automaticos/{n}", "Generar reportes automáticos para las últimas N semanas",
                "GET /api/reportes/por-año/{año}", "Obtener reportes de un año específico",
                "GET /api/reportes/ultimos/{limite}", "Obtener los últimos N reportes",
                "GET /api/reportes/diario-ganancias", "Generar reporte diario de ganancias",
                "GET /api/reportes/exportar-diario-ganancias", "Exportar reporte diario de ganancias a Excel"));

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Generar reporte diario de ganancias", description = "Genera un reporte de ganancias para un día específico (solo pedidos entregados)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reporte generado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Fecha inválida"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/diario-ganancias")
    public ResponseEntity<Map<String, Object>> generarReporteDiarioGanancias(
            @Parameter(description = "Fecha del día (formato: YYYY-MM-DD)", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        Map<String, Object> response = new HashMap<>();

        try {
            Map<String, Object> reporte = reporteService.generarReporteDiarioGanancias(fecha);

            response.put("status", "SUCCESS");
            response.put("message", "Reporte diario generado exitosamente");
            response.put("reporte", reporte);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Error al generar reporte diario: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @Operation(summary = "Exportar reporte diario de ganancias a Excel", description = "Genera y descarga un archivo Excel con el reporte diario de ganancias")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Archivo Excel generado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Fecha inválida"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/exportar-diario-ganancias")
    public ResponseEntity<byte[]> exportarReporteDiarioGanancias(
            @Parameter(description = "Fecha del día (formato: YYYY-MM-DD)", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha)
            throws IOException {

        try {
            Map<String, Object> reporte = reporteService.generarReporteDiarioGanancias(fecha);
            byte[] excelBytes = excelExportService.exportarReporteDiarioGanancias(reporte);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "reporte_ganancias_" + fecha + ".xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error al exportar reporte: " + e.getMessage());
        }
    }
}