package com.farma_ya.controller;

import com.farma_ya.model.Order;
import com.farma_ya.model.OrderStatus;
import com.farma_ya.model.Role;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
            @Parameter(description = "ID del pedido", required = true) @PathVariable Integer id,
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
            @Parameter(description = "ID del pedido", required = true) @PathVariable Integer id,
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
    public ResponseEntity<List<Map<String, Object>>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        List<Map<String, Object>> simplifiedOrders = orders.stream().map(order -> {
            Map<String, Object> orderData = new HashMap<>();
            orderData.put("id", order.getId());
            orderData.put("numeroPedido", order.getNumeroPedido());
            orderData.put("createdAt", new int[] {
                    order.getCreatedAt().getYear(),
                    order.getCreatedAt().getMonthValue(),
                    order.getCreatedAt().getDayOfMonth(),
                    order.getCreatedAt().getHour(),
                    order.getCreatedAt().getMinute(),
                    order.getCreatedAt().getSecond()
            });
            orderData.put("status", order.getStatus().name());
            orderData.put("totalAmount", order.getTotalAmount());
            orderData.put("calculatedTotalAmount", order.getCalculatedTotalAmount());
            orderData.put("shippingAddress", order.getShippingAddressLine());
            orderData.put("shippingDistrict", order.getShippingDistrict());
            orderData.put("shippingReference", order.getShippingReference());

            // Convertir estado a inglés para compatibilidad con frontend
            String statusEnglish = convertStatusToEnglish(order.getStatus());
            orderData.put("status", statusEnglish);

            // Items del pedido (simplificados)
            List<Map<String, Object>> items = order.getItems().stream().map(item -> {
                Map<String, Object> itemData = new HashMap<>();
                itemData.put("id", item.getId());
                itemData.put("quantity", item.getQuantity());
                itemData.put("price", item.getPrice());
                itemData.put("subtotal", item.getSubtotal());
                if (item.getProduct() != null) {
                    Map<String, Object> productData = new HashMap<>();
                    productData.put("id", item.getProduct().getId());
                    productData.put("name", item.getProduct().getName());
                    itemData.put("product", productData);
                }
                return itemData;
            }).collect(Collectors.toList());
            orderData.put("items", items);

            // Usuario
            if (order.getUser() != null) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("id", order.getUser().getId());
                userData.put("username", order.getUser().getUsername());
                userData.put("email", order.getUser().getEmail());
                userData.put("telefono", order.getUser().getTelefono());
                orderData.put("user", userData);
            }

            // Repartidor
            if (order.getRepartidor() != null) {
                Map<String, Object> deliveryData = new HashMap<>();
                deliveryData.put("id", order.getRepartidor().getId());
                deliveryData.put("username", order.getRepartidor().getUsername());
                orderData.put("repartidor", deliveryData);
            }

            return orderData;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(simplifiedOrders);
    }

    @Operation(summary = "Asignar repartidor a pedido", description = "Asigna un repartidor a un pedido (solo administradores)")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/assign-delivery")
    public ResponseEntity<Order> assignDelivery(
            @PathVariable Integer id,
            @RequestParam Integer repartidorId) {
        User repartidor = userService.getUserById(repartidorId);
        if (repartidor.getRole() != Role.DELIVERY) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Order updatedOrder = orderService.assignRepartidor(id, repartidor);
        return ResponseEntity.ok(updatedOrder);
    }

    @Operation(summary = "Obtener pedidos por repartidor", description = "Retorna pedidos asignados a un repartidor (admin o el repartidor mismo)")
    @GetMapping("/delivery/{repartidorId}")
    public ResponseEntity<List<Order>> getOrdersByDelivery(
            @PathVariable Integer repartidorId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        User repartidor = userService.getUserById(repartidorId);

        // Solo admin puede ver pedidos de otros repartidores
        if (!currentUser.getId().equals(repartidorId) && currentUser.getRole() != Role.ADMIN) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        List<Order> orders = orderService.getOrdersByRepartidor(repartidor);
        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "Actualizar estado por repartidor", description = "Permite a un repartidor actualizar el estado de sus pedidos asignados")
    @PreAuthorize("hasRole('DELIVERY')")
    @PutMapping("/{id}/delivery-status")
    public ResponseEntity<Order> updateDeliveryStatus(
            @PathVariable Integer id,
            @RequestParam String status,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        Order order = orderService.getOrderById(id);

        // Verificar que el pedido esté asignado a este repartidor
        if (order.getRepartidor() == null || !order.getRepartidor().getId().equals(currentUser.getId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Order updatedOrder = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(updatedOrder);
    }

    @Operation(summary = "Obtener pedidos sin asignar", description = "Retorna pedidos pendientes sin repartidor asignado (solo administradores)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/unassigned")
    public ResponseEntity<List<Order>> getUnassignedOrders() {
        List<Order> orders = orderService.getUnassignedOrdersByStatus(OrderStatus.PENDIENTE);
        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "Obtener repartidores disponibles", description = "Retorna lista de repartidores para asignar pedidos (solo administradores)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/delivery/available")
    public ResponseEntity<List<User>> getAvailableDeliveryUsers() {
        List<User> deliveryUsers = userService.getUsersByRole(Role.DELIVERY);
        return ResponseEntity.ok(deliveryUsers);
    }

    // TEMPORAL: Endpoint público para debugging - REMOVER EN PRODUCCIÓN
    @Operation(summary = "Obtener repartidores disponibles (público)", description = "Retorna lista de repartidores - SOLO PARA DEBUGGING")
    @GetMapping("/delivery/available-public")
    public ResponseEntity<List<User>> getAvailableDeliveryUsersPublic() {
        List<User> deliveryUsers = userService.getUsersByRole(Role.DELIVERY);
        return ResponseEntity.ok(deliveryUsers);
    }

    @Operation(summary = "Obtener pedidos de un usuario", description = "Retorna todos los pedidos de un usuario específico (solo administradores)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getOrdersByUserId(@PathVariable Integer userId) {
        User user = userService.getUserById(userId);
        List<Order> orders = orderService.getOrdersByUser(user);
        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "Obtener mis pedidos asignados", description = "Retorna pedidos asignados al repartidor autenticado")
    @PreAuthorize("hasRole('DELIVERY')")
    @GetMapping("/delivery/my-orders")
    public ResponseEntity<List<Order>> getMyAssignedOrders(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        List<Order> orders = orderService.getOrdersByRepartidor(currentUser);
        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "Obtener estadísticas del repartidor", description = "Retorna estadísticas de entregas del repartidor autenticado")
    @PreAuthorize("hasRole('DELIVERY')")
    @GetMapping("/delivery/stats")
    public ResponseEntity<Map<String, Object>> getDeliveryStats(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        Map<String, Object> stats = orderService.getDeliveryStats(currentUser);
        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "Obtener detalle de pedido asignado", description = "Permite a un repartidor ver detalles de un pedido que le está asignado")
    @PreAuthorize("hasRole('DELIVERY')")
    @GetMapping("/delivery/order/{id}")
    public ResponseEntity<Order> getAssignedOrderDetail(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        Order order = orderService.getOrderById(id);

        // Verificar que el pedido esté asignado a este repartidor
        if (order.getRepartidor() == null || !order.getRepartidor().getId().equals(currentUser.getId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        return ResponseEntity.ok(order);
    }

    // Método auxiliar para convertir estados del enum español a inglés para el
    // frontend
    private String convertStatusToEnglish(OrderStatus status) {
        switch (status) {
            case PENDIENTE:
                return "PENDING";
            case PROCESANDO:
                return "PROCESSING";
            case ENVIADO:
                return "DELIVERED";
            case ENTREGADO:
                return "DELIVERED";
            case CANCELADO:
                return "CANCELLED";
            default:
                return status.name();
        }
    }
}