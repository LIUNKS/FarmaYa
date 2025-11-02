package com.farma_ya.controller;

import com.farma_ya.model.Order;
import com.farma_ya.model.OrderStatus;
import com.farma_ya.model.Product;
import com.farma_ya.model.Role;
import com.farma_ya.service.IOrderService;
import com.farma_ya.service.IProductService;
import com.farma_ya.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Dashboard", description = "Estadísticas y métricas del sistema")
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final IOrderService orderService;
    private final IProductService productService;
    private final UserService userService;

    public DashboardController(IOrderService orderService, IProductService productService, UserService userService) {
        this.orderService = orderService;
        this.productService = productService;
        this.userService = userService;
    }

    @Operation(summary = "Obtener estadísticas generales", description = "Retorna métricas principales del dashboard (solo administradores)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // Total productos
        stats.put("totalProductos", productService.getAllProducts().size());

        // Total pedidos
        stats.put("totalPedidos", orderService.getAllOrders().size());

        // Pedidos por estado
        Map<String, Long> pedidosPorEstado = new HashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            pedidosPorEstado.put(status.name(), orderService.countOrdersByStatus(status));
        }
        stats.put("pedidosPorEstado", pedidosPorEstado);

        // Total usuarios
        stats.put("totalUsuarios", userService.getAllUsers().size());

        // Usuarios por rol
        Map<String, Long> usuariosPorRol = new HashMap<>();
        for (Role role : Role.values()) {
            usuariosPorRol.put(role.name(), userService.countUsersByRole(role));
        }
        stats.put("usuariosPorRol", usuariosPorRol);

        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "Obtener productos con bajo stock", description = "Retorna productos con stock menor o igual a 10 (solo administradores)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/low-stock")
    public ResponseEntity<List<Product>> getLowStockProducts() {
        // Asumiendo que bajo stock es <= 10
        List<Product> products = productService.getAllProducts().stream()
                .filter(p -> p.getStock() <= 10)
                .toList();
        return ResponseEntity.ok(products);
    }

    @Operation(summary = "Obtener pedidos recientes", description = "Retorna los últimos 10 pedidos (solo administradores)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/recent-orders")
    public ResponseEntity<List<Order>> getRecentOrders() {
        List<Order> orders = orderService.getRecentOrders(10);
        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "Obtener datos completos del dashboard", description = "Retorna toda la información necesaria para el dashboard administrativo (solo administradores)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/data")
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        Map<String, Object> dashboardData = new HashMap<>();

        // Estadísticas básicas
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProductos", productService.getAllProducts().size());
        stats.put("totalPedidos", orderService.getAllOrders().size());
        stats.put("totalUsuarios", userService.getAllUsers().size());

        // Contar pedidos por estado
        Map<String, Long> pedidosPorEstado = new HashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            pedidosPorEstado.put(status.name(), orderService.countOrdersByStatus(status));
        }
        stats.put("pedidosPorEstado", pedidosPorEstado);

        dashboardData.put("stats", stats);

        // Productos con poco stock (stock <= 20)
        List<Product> lowStockProducts = productService.getAllProducts().stream()
                .filter(p -> p.getStock() <= 20)
                .sorted((a, b) -> Integer.compare(a.getStock(), b.getStock()))
                .toList();
        dashboardData.put("lowStockProducts", lowStockProducts);

        // Pedidos recientes (últimos 10)
        List<Order> recentOrders = orderService.getRecentOrders(10);
        dashboardData.put("recentOrders", recentOrders);

        return ResponseEntity.ok(dashboardData);
    }

    @Operation(summary = "Obtener estadísticas del admin", description = "Retorna estadísticas de gestión del administrador")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/stats")
    public ResponseEntity<Map<String, Object>> getAdminStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProductos", productService.getAllProducts().size());
        stats.put("totalPedidos", orderService.getAllOrders().size());
        stats.put("totalUsuarios", userService.getAllUsers().size());
        return ResponseEntity.ok(stats);
    }
}