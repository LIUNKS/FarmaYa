package com.farma_ya.service;

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
            row.createCell(0).setCellValue((Integer) pedido.get("id"));
            row.createCell(1).setCellValue((String) pedido.get("numeroPedido"));
            Cell montoCell = row.createCell(2);
            montoCell.setCellValue((Double) pedido.get("totalAmount"));
            montoCell.setCellStyle(currencyStyle);
            row.createCell(3).setCellValue(pedido.get("createdAt").toString());
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
}