package com.farma_ya.service;

import com.farma_ya.model.DetalleVentaSemanalProducto;
import com.farma_ya.model.ReporteVentaSemanal;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class ExcelExportService {

    public byte[] exportarReporteDiarioGanancias(Map<String, Object> reporte) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Reporte Diario Ganancias");

        // Crear estilos
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        CellStyle currencyStyle = workbook.createCellStyle();
        currencyStyle.setDataFormat(workbook.createDataFormat().getFormat("$#,##0.00"));

        // Fila de resumen
        Row summaryRow = sheet.createRow(0);
        summaryRow.createCell(0).setCellValue("Fecha");
        summaryRow.createCell(1).setCellValue("Total Ganancias");
        summaryRow.createCell(2).setCellValue("Total Pedidos");
        summaryRow.createCell(3).setCellValue("Total Productos Vendidos");

        for (int i = 0; i < 4; i++) {
            summaryRow.getCell(i).setCellStyle(headerStyle);
        }

        Row dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue(reporte.get("fecha").toString());
        Cell gananciasCell = dataRow.createCell(1);
        gananciasCell.setCellValue(((BigDecimal) reporte.get("totalGanancias")).doubleValue());
        gananciasCell.setCellStyle(currencyStyle);
        dataRow.createCell(2).setCellValue((Integer) reporte.get("totalPedidos"));
        dataRow.createCell(3).setCellValue((Integer) reporte.get("totalProductosVendidos"));

        // Detalles de pedidos
        Row headerPedidosRow = sheet.createRow(3);
        headerPedidosRow.createCell(0).setCellValue("ID Pedido");
        headerPedidosRow.createCell(1).setCellValue("Número Pedido");
        headerPedidosRow.createCell(2).setCellValue("Monto");
        headerPedidosRow.createCell(3).setCellValue("Fecha Creación");

        for (int i = 0; i < 4; i++) {
            headerPedidosRow.getCell(i).setCellStyle(headerStyle);
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> pedidos = (List<Map<String, Object>>) reporte.get("pedidos");
        int rowNum = 4;
        for (Map<String, Object> pedido : pedidos) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(pedido.get("id") != null ? ((Integer) pedido.get("id")).doubleValue() : 0.0);
            row.createCell(1).setCellValue((String) pedido.get("numeroPedido"));
            Cell montoCell = row.createCell(2);
            Object totalAmount = pedido.get("totalAmount");
            double amount = 0.0;
            if (totalAmount instanceof Double) {
                amount = (Double) totalAmount;
            } else if (totalAmount instanceof BigDecimal) {
                amount = ((BigDecimal) totalAmount).doubleValue();
            } else if (totalAmount instanceof Number) {
                amount = ((Number) totalAmount).doubleValue();
            }
            montoCell.setCellValue(amount);
            montoCell.setCellStyle(currencyStyle);
            row.createCell(3).setCellValue(pedido.get("createdAt") != null ? pedido.get("createdAt").toString() : "");
        }

        // Autoajustar columnas
        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }

        // Escribir a bytes
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        return outputStream.toByteArray();
    }

    public byte[] exportarReporteSemanal(ReporteVentaSemanal reporte) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet resumenSheet = workbook.createSheet("Resumen Semanal");
        Sheet detallesSheet = workbook.createSheet("Detalles por Producto");

        // Crear estilos
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        CellStyle currencyStyle = workbook.createCellStyle();
        currencyStyle.setDataFormat(workbook.createDataFormat().getFormat("$#,##0.00"));

        // === HOJA DE RESUMEN ===
        // Encabezado
        Row titleRow = resumenSheet.createRow(0);
        titleRow.createCell(0).setCellValue("REPORTE SEMANAL DE VENTAS");
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);
        titleStyle.setFont(titleFont);
        titleRow.getCell(0).setCellStyle(titleStyle);

        // Información del período
        Row periodoRow = resumenSheet.createRow(2);
        periodoRow.createCell(0).setCellValue("Período:");
        periodoRow.createCell(1).setCellValue(reporte.getSemanaInicio() + " - " + reporte.getSemanaFin());

        Row semanaRow = resumenSheet.createRow(3);
        semanaRow.createCell(0).setCellValue("Semana:");
        semanaRow.createCell(1).setCellValue(reporte.getYearSemana());

        Row generadoRow = resumenSheet.createRow(4);
        generadoRow.createCell(0).setCellValue("Generado:");
        generadoRow.createCell(1).setCellValue(reporte.getGeneradoEn().toString());

        // Métricas principales
        Row headerMetricas = resumenSheet.createRow(6);
        headerMetricas.createCell(0).setCellValue("MÉTRICA");
        headerMetricas.createCell(1).setCellValue("VALOR");
        headerMetricas.getCell(0).setCellStyle(headerStyle);
        headerMetricas.getCell(1).setCellStyle(headerStyle);

        Row totalPedidosRow = resumenSheet.createRow(7);
        totalPedidosRow.createCell(0).setCellValue("Total Pedidos");
        totalPedidosRow.createCell(1).setCellValue(reporte.getTotalPedidos());

        Row totalProductosRow = resumenSheet.createRow(8);
        totalProductosRow.createCell(0).setCellValue("Total Productos Vendidos");
        totalProductosRow.createCell(1).setCellValue(reporte.getTotalProductosVendidos());

        Row totalIngresosRow = resumenSheet.createRow(9);
        totalIngresosRow.createCell(0).setCellValue("Total Ingresos");
        Cell ingresosCell = totalIngresosRow.createCell(1);
        ingresosCell.setCellValue(reporte.getTotalIngresos().doubleValue());
        ingresosCell.setCellStyle(currencyStyle);

        // Producto más vendido
        Row productoMasVendidoRow = resumenSheet.createRow(11);
        productoMasVendidoRow.createCell(0).setCellValue("Producto Más Vendido");
        if (reporte.getProductoMasVendido() != null) {
            productoMasVendidoRow.createCell(1).setCellValue(reporte.getProductoMasVendido().getName());
        } else {
            productoMasVendidoRow.createCell(1).setCellValue("N/A");
        }

        // Categoría más vendida
        Row categoriaMasVendidaRow = resumenSheet.createRow(12);
        categoriaMasVendidaRow.createCell(0).setCellValue("Categoría Más Vendida");
        categoriaMasVendidaRow.createCell(1)
                .setCellValue(reporte.getCategoriaMasVendida() != null ? reporte.getCategoriaMasVendida() : "N/A");

        // Autoajustar columnas en resumen
        resumenSheet.autoSizeColumn(0);
        resumenSheet.autoSizeColumn(1);

        // === HOJA DE DETALLES POR PRODUCTO ===
        Row headerDetalles = detallesSheet.createRow(0);
        headerDetalles.createCell(0).setCellValue("Producto");
        headerDetalles.createCell(1).setCellValue("Categoría");
        headerDetalles.createCell(2).setCellValue("Cantidad Vendida");
        headerDetalles.createCell(3).setCellValue("Total Ingresos");

        for (int i = 0; i < 4; i++) {
            headerDetalles.getCell(i).setCellStyle(headerStyle);
        }

        // Datos de productos
        List<DetalleVentaSemanalProducto> detalles = reporte.getDetallesProductos();
        if (detalles != null) {
            int rowNum = 1;
            for (DetalleVentaSemanalProducto detalle : detalles) {
                Row row = detallesSheet.createRow(rowNum++);
                row.createCell(0).setCellValue(detalle.getProducto().getName());
                row.createCell(1).setCellValue(
                        detalle.getProducto().getCategoria() != null ? detalle.getProducto().getCategoria()
                                : "Sin categoría");
                row.createCell(2).setCellValue(detalle.getCantidadVendida());
                Cell ingresosDetalleCell = row.createCell(3);
                ingresosDetalleCell.setCellValue(detalle.getTotalIngresos().doubleValue());
                ingresosDetalleCell.setCellStyle(currencyStyle);
            }
        }

        // Autoajustar columnas en detalles
        for (int i = 0; i < 4; i++) {
            detallesSheet.autoSizeColumn(i);
        }

        // Escribir a bytes
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        return outputStream.toByteArray();
    }
}