package com.farma_ya.integration;

import com.farma_ya.exception.GlobalExceptionHandler;
import com.farma_ya.model.*;
import com.farma_ya.service.IOrderService;
import com.farma_ya.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({ GlobalExceptionHandler.class })
public class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IOrderService orderService;

    @MockitoBean
    private UserService userService;

    private User testUser;
    private User deliveryUser;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setRole(Role.USER);

        deliveryUser = new User();
        deliveryUser.setId(2);
        deliveryUser.setUsername("delivery");
        deliveryUser.setEmail("delivery@example.com");
        deliveryUser.setRole(Role.DELIVERY);

        testOrder = new Order();
        testOrder.setId(1);
        testOrder.setUser(testUser);
        testOrder.setStatus(OrderStatus.PENDIENTE);
        testOrder.setTotalAmount(BigDecimal.valueOf(100.0));
        testOrder.setNumeroPedido("ORD-001");
    }

    @Test
    @WithMockUser(username = "testuser")
    void getUserOrders_ShouldReturnUserOrders() throws Exception {
        // Given
        List<Order> orders = Arrays.asList(testOrder);
        when(userService.getUserByUsername("testuser")).thenReturn(testUser);
        when(orderService.getOrdersByUser(testUser)).thenReturn(orders);

        // When & Then
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].numeroPedido").value("ORD-001"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getOrderById_UserOwnsOrder_ShouldReturnOrder() throws Exception {
        // Given
        when(userService.getUserByUsername("testuser")).thenReturn(testUser);
        when(orderService.getOrderById(1)).thenReturn(testOrder);

        // When & Then
        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.numeroPedido").value("ORD-001"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllOrders_AdminAccess_ShouldReturnAllOrders() throws Exception {
        // Given
        List<Order> orders = Arrays.asList(testOrder);
        when(orderService.getAllOrders()).thenReturn(orders);

        // When & Then
        mockMvc.perform(get("/api/orders/admin/all"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateOrderStatus_ValidStatus_ShouldUpdateSuccessfully() throws Exception {
        // Given
        Order updatedOrder = new Order();
        updatedOrder.setId(1);
        updatedOrder.setStatus(OrderStatus.PROCESANDO);

        when(orderService.updateOrderStatus(1, "PROCESANDO")).thenReturn(updatedOrder);

        // When & Then
        mockMvc.perform(put("/api/orders/1/status")
                .param("status", "PROCESANDO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PROCESANDO"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void assignDelivery_ValidAssignment_ShouldAssignSuccessfully() throws Exception {
        // Given
        when(userService.getUserById(2)).thenReturn(deliveryUser);
        when(orderService.assignRepartidor(1, deliveryUser)).thenReturn(testOrder);

        // When & Then
        mockMvc.perform(put("/api/orders/1/assign-delivery")
                .param("repartidorId", "2"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "delivery", roles = { "DELIVERY" })
    void getMyAssignedOrders_DeliveryUser_ShouldReturnAssignedOrders() throws Exception {
        // Given
        testOrder.setRepartidor(deliveryUser);
        List<Order> orders = Arrays.asList(testOrder);

        when(userService.getUserByUsername("delivery")).thenReturn(deliveryUser);
        when(orderService.getOrdersByRepartidor(deliveryUser)).thenReturn(orders);

        // When & Then
        mockMvc.perform(get("/api/orders/delivery/my-orders"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @WithMockUser(username = "delivery", roles = { "DELIVERY" })
    void getDeliveryStats_DeliveryUser_ShouldReturnStats() throws Exception {
        // Given
        when(userService.getUserByUsername("delivery")).thenReturn(deliveryUser);
        when(orderService.getDeliveryStats(deliveryUser)).thenReturn(java.util.Map.of(
                "pedidosPendientes", 2,
                "pedidosEnProceso", 1,
                "pedidosEntregados", 5,
                "totalGanancias", 150.0));

        // When & Then
        mockMvc.perform(get("/api/orders/delivery/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pedidosPendientes").value(2))
                .andExpect(jsonPath("$.pedidosEnProceso").value(1))
                .andExpect(jsonPath("$.pedidosEntregados").value(5))
                .andExpect(jsonPath("$.totalGanancias").value(150.0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAvailableDeliveryUsers_ShouldReturnDeliveryUsers() throws Exception {
        // Given
        List<User> deliveryUsers = Arrays.asList(deliveryUser);
        when(userService.getUsersByRole(Role.DELIVERY)).thenReturn(deliveryUsers);

        // When & Then
        mockMvc.perform(get("/api/orders/delivery/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username").value("delivery"));
    }
}