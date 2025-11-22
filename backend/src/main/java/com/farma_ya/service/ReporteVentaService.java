package com.farma_ya.service;

import com.farma_ya.model.DetalleVentaSemanalProducto;
import com.farma_ya.model.Order;
import com.farma_ya.model.OrderItem;
import com.farma_ya.model.Product;
import com.farma_ya.model.ReporteVentaSemanal;
import com.farma_ya.repository.DetalleVentaSemanalProductoRepository;
import com.farma_ya.repository.OrderRepository;
import com.farma_ya.repository.ProductRepository;
import com.farma_ya.repository.ReporteVentaSemanalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReporteVentaService {

    @Autowired
    private ReporteVentaSemanalRepository reporteRepository;

    @Autowired
    private DetalleVentaSemanalProductoRepository detalleRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    /**
     * Genera un reporte de ventas para una semana específica
     */
    public ReporteVentaSemanal generarReporteSemanal(LocalDate fechaInicio, LocalDate fechaFin) {
        String yearSemana = crearIdentificadorSemana(fechaInicio);

        // Verificar si ya existe el reporte
        Optional<ReporteVentaSemanal> reporteExistente = reporteRepository.findByYearSemana(yearSemana);
        if (reporteExistente.isPresent()) {
            return reporteExistente.get();
        }

        // Crear nuevo reporte
        ReporteVentaSemanal reporte = new ReporteVentaSemanal(fechaInicio, fechaFin, yearSemana);

        // Obtener pedidos de la semana (solo entregados)
        List<Order> pedidosSemana = orderRepository.findPedidosEntregadosPorRangoFechas(fechaInicio, fechaFin);

        // Calcular métricas principales
        reporte.setTotalPedidos(pedidosSemana.size());

        BigDecimal totalIngresos = BigDecimal.ZERO;
        int totalProductosVendidos = 0;
        Map<Integer, Integer> ventasPorProducto = new HashMap<>();
        Map<String, Integer> ventasPorCategoria = new HashMap<>();

        for (Order pedido : pedidosSemana) {
            totalIngresos = totalIngresos.add(BigDecimal.valueOf(pedido.getTotalAmount()));

            for (OrderItem item : pedido.getItems()) {
                totalProductosVendidos += item.getQuantity();

                // Contar ventas por producto
                Integer productoId = item.getProduct().getId();
                ventasPorProducto.merge(productoId, item.getQuantity(), Integer::sum);

                // Contar ventas por categoría
                String categoria = item.getProduct().getCategoria();
                if (categoria != null) {
                    ventasPorCategoria.merge(categoria, item.getQuantity(), Integer::sum);
                }
            }
        }

        reporte.setTotalIngresos(totalIngresos);
        reporte.setTotalProductosVendidos(totalProductosVendidos);

        // Encontrar producto más vendido
        if (!ventasPorProducto.isEmpty()) {
            Integer productoMasVendidoId = ventasPorProducto.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);

            if (productoMasVendidoId != null) {
                productRepository.findById(productoMasVendidoId)
                        .ifPresent(reporte::setProductoMasVendido);
            }
        }

        // Encontrar categoría más vendida
        if (!ventasPorCategoria.isEmpty()) {
            String categoriaMasVendida = ventasPorCategoria.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);
            reporte.setCategoriaMasVendida(categoriaMasVendida);
        }

        // Guardar reporte principal
        reporte = reporteRepository.save(reporte);

        // Crear detalles por producto
        crearDetallesProducto(reporte, pedidosSemana);

        return reporte;
    }

    /**
     * Genera reportes automáticamente para las últimas semanas que no tienen
     * reporte
     */
    public List<ReporteVentaSemanal> generarReportesAutomaticos(int numeroSemanas) {
        List<ReporteVentaSemanal> reportesGenerados = new ArrayList<>();
        LocalDate fechaActual = LocalDate.now();

        for (int i = 0; i < numeroSemanas; i++) {
            LocalDate inicioSemana = fechaActual.minusWeeks(i).with(WeekFields.ISO.dayOfWeek(), 1);
            LocalDate finSemana = inicioSemana.plusDays(6);

            String yearSemana = crearIdentificadorSemana(inicioSemana);

            // Solo generar si no existe
            if (!reporteRepository.existsByYearSemana(yearSemana)) {
                ReporteVentaSemanal reporte = generarReporteSemanal(inicioSemana, finSemana);
                reportesGenerados.add(reporte);
            }
        }

        return reportesGenerados;
    }

    /**
     * Obtiene reportes por año
     */
    public List<ReporteVentaSemanal> getReportesPorAno(int año) {
        return reporteRepository.findByYear(String.valueOf(año));
    }

    /**
     * Obtiene los últimos N reportes
     */
    public List<ReporteVentaSemanal> getUltimosReportes(int limite) {
        return reporteRepository.findTopReportes(limite);
    }

    /**
     * Obtiene un reporte por su ID
     */
    public ReporteVentaSemanal getReporteById(Long id) {
        Optional<ReporteVentaSemanal> reporteOpt = reporteRepository.findById(id);
        if (reporteOpt.isPresent()) {
            ReporteVentaSemanal reporte = reporteOpt.get();
            // Forzar carga de detalles lazy
            if (reporte.getDetallesProductos() != null) {
                reporte.getDetallesProductos().size(); // Forzar inicialización
            }
            return reporte;
        }
        return null;
    }

    /**
     * Genera un reporte diario de ganancias para un día específico
     */
    public Map<String, Object> generarReporteDiarioGanancias(LocalDate fecha) {
        try {
            // Obtener pedidos entregados del día
            List<Order> pedidosDia = orderRepository.findPedidosEntregadosPorRangoFechas(fecha, fecha);

            // Calcular ganancias totales
            BigDecimal totalGanancias = pedidosDia.stream()
                    .map(order -> BigDecimal.valueOf(order.getTotalAmount()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Número de pedidos
            int totalPedidos = pedidosDia.size();

            // Productos vendidos
            int totalProductosVendidos = pedidosDia.stream()
                    .mapToInt(order -> order.getItems().stream().mapToInt(OrderItem::getQuantity).sum())
                    .sum();

            // Crear mapa de respuesta
            Map<String, Object> reporte = new HashMap<>();
            reporte.put("fecha", fecha);
            reporte.put("totalGanancias", totalGanancias);
            reporte.put("totalPedidos", totalPedidos);
            reporte.put("totalProductosVendidos", totalProductosVendidos);
            reporte.put("pedidos", pedidosDia.stream().map(order -> {
                Map<String, Object> orderData = new HashMap<>();
                orderData.put("id", order.getId());
                orderData.put("numeroPedido", order.getNumeroPedido());
                orderData.put("totalAmount", order.getTotalAmount()); // Ya es double
                orderData.put("createdAt", order.getCreatedAt());
                return orderData;
            }).collect(Collectors.toList()));

            return reporte;
        } catch (Exception e) {
            throw e;
        }
    }

    private void crearDetallesProducto(ReporteVentaSemanal reporte, List<Order> pedidosSemana) {
        Map<Integer, DetalleData> detallesPorProducto = new HashMap<>();

        for (Order pedido : pedidosSemana) {
            for (OrderItem item : pedido.getItems()) {
                Integer productoId = item.getProduct().getId();
                BigDecimal ingresoItem = item.getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity()));

                detallesPorProducto.merge(productoId,
                        new DetalleData(item.getProduct(), item.getQuantity(), ingresoItem),
                        (existing, nuevo) -> new DetalleData(
                                existing.producto,
                                existing.cantidad + nuevo.cantidad,
                                existing.ingresos.add(nuevo.ingresos)));
            }
        }

        // Crear y guardar detalles
        List<DetalleVentaSemanalProducto> detalles = detallesPorProducto.values().stream()
                .map(data -> new DetalleVentaSemanalProducto(reporte, data.producto, data.cantidad, data.ingresos))
                .collect(Collectors.toList());

        detalleRepository.saveAll(detalles);
    }

    private String crearIdentificadorSemana(LocalDate fecha) {
        int año = fecha.getYear();
        int semana = fecha.get(WeekFields.ISO.weekOfYear());
        return String.format("%d-W%02d", año, semana);
    }

    // Clase auxiliar para acumular datos
    private static class DetalleData {
        Product producto;
        Integer cantidad;
        BigDecimal ingresos;

        DetalleData(Product producto, Integer cantidad, BigDecimal ingresos) {
            this.producto = producto;
            this.cantidad = cantidad;
            this.ingresos = ingresos;
        }
    }
}