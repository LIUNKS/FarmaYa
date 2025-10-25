package com.farma_ya.controller;

import com.farma_ya.model.Order;
import com.farma_ya.model.User;
import com.farma_ya.service.IOrderService;
import com.farma_ya.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Pedidos", description = "Gestión de pedidos y órdenes de compra")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final IOrderService orderService;

    private final UserService userService;

    public OrderController(IOrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    @Operation(summary = "Crear pedido", description = "Crea un nuevo pedido a partir del carrito actual del usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pedido creado exitosamente", content = @Content(schema = @Schema(implementation = Order.class))),
            @ApiResponse(responseCode = "400", description = "Carrito vacío o productos sin stock"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado")
    })
    @PostMapping
    public ResponseEntity<Order> createOrder(@AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> shippingData) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        Order newOrder = orderService.createOrderFromCart(currentUser, shippingData);
        return new ResponseEntity<>(newOrder, HttpStatus.CREATED);
    }

    @Operation(summary = "Obtener pedidos del usuario", description = "Retorna todos los pedidos del usuario autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de pedidos obtenida exitosamente", content = @Content(schema = @Schema(implementation = Order.class))),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado")
    })
    @GetMapping
    public ResponseEntity<List<Order>> getUserOrders(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        List<Order> orders = orderService.getOrdersByUser(currentUser);
        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "Obtener pedido por ID", description = "Obtiene los detalles de un pedido específico (solo el propietario)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido obtenido exitosamente", content = @Content(schema = @Schema(implementation = Order.class))),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - No es propietario del pedido"),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(
            @Parameter(description = "ID del pedido", required = true) @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        Order order = orderService.getOrderById(id);
        if (!order.getUser().getId().equals(currentUser.getId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return ResponseEntity.ok(order);
    }

    @Operation(summary = "Actualizar estado del pedido", description = "Actualiza el estado de un pedido (solo administradores)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado del pedido actualizado exitosamente", content = @Content(schema = @Schema(implementation = Order.class))),
            @ApiResponse(responseCode = "400", description = "Estado inválido"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Solo administradores"),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @Parameter(description = "ID del pedido", required = true) @PathVariable Long id,
            @Parameter(description = "Nuevo estado del pedido (PENDING, PROCESSING, DELIVERED, CANCELLED)", required = true) @RequestParam String status) {
        Order updatedOrder = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(updatedOrder);
    }

    @Operation(summary = "Obtener todos los pedidos", description = "Retorna todos los pedidos del sistema (solo administradores)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de pedidos obtenida exitosamente", content = @Content(schema = @Schema(implementation = Order.class))),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Solo administradores")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/all")
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }
}