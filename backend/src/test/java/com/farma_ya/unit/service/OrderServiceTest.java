package com.farma_ya.unit.service;

import com.farma_ya.exception.ResourceNotFoundException;
import com.farma_ya.model.*;
import com.farma_ya.repository.DireccionRepository;
import com.farma_ya.repository.OrderRepository;
import com.farma_ya.service.ICartService;
import com.farma_ya.service.InventoryService;
import com.farma_ya.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ICartService cartService;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private DireccionRepository direccionRepository;

    @InjectMocks
    private OrderService orderService;

    private User testUser;
    private Order testOrder;
    private Cart testCart;

    @BeforeEach
    void setUp() {
        testUser = new User(1, "testuser", "test@example.com", "password", Role.USER);

        testCart = new Cart();
        testCart.setId(1);
        testCart.setUser(testUser);

        testOrder = new Order();
        testOrder.setId(1);
        testOrder.setUser(testUser);
        testOrder.setStatus(OrderStatus.PENDIENTE);
        testOrder.setTotalAmount(BigDecimal.valueOf(100.0));
    }

    @Test
    void getOrderById_ExistingOrder_ReturnsOrder() {
        // Given
        when(orderRepository.findById(1)).thenReturn(Optional.of(testOrder));

        // When
        Order result = orderService.getOrderById(1);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        verify(orderRepository).findById(1);
    }

    @Test
    void getOrderById_NonExistingOrder_ThrowsException() {
        // Given
        when(orderRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> orderService.getOrderById(999))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Orden no encontrada con ID: 999");
    }

    @Test
    void updateOrderStatus_ValidStatus_UpdatesSuccessfully() {
        // Given
        when(orderRepository.findById(1)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // When
        Order result = orderService.updateOrderStatus(1, "PROCESANDO");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PROCESANDO);
        verify(orderRepository).save(testOrder);
    }

    @Test
    void updateOrderStatus_InvalidStatus_ThrowsException() {
        // Given
        when(orderRepository.findById(1)).thenReturn(Optional.of(testOrder));

        // When & Then
        assertThatThrownBy(() -> orderService.updateOrderStatus(1, "INVALID_STATUS"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Estado de orden inválido");
    }

    @Test
    void updateOrderStatus_FrontendStatusConversion_ConvertsCorrectly() {
        // Given
        when(orderRepository.findById(1)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // When - Simular envío desde frontend con "EN_PROCESO"
        Order result = orderService.updateOrderStatus(1, "EN_PROCESO");

        // Then - Debe convertirse a PROCESANDO
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PROCESANDO);
        verify(orderRepository).save(testOrder);
    }

    @Test
    void getOrdersByRepartidor_ReturnsOrdersForDeliveryUser() {
        // Given
        User repartidor = new User(2, "repartidor", "rep@example.com", "pass", Role.DELIVERY);
        List<Order> expectedOrders = Arrays.asList(testOrder);

        when(orderRepository.findByRepartidor(repartidor)).thenReturn(expectedOrders);

        // When
        List<Order> result = orderService.getOrdersByRepartidor(repartidor);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1);
        verify(orderRepository).findByRepartidor(repartidor);
    }

    @Test
    void getDeliveryStats_CalculatesCorrectStats() {
        // Given
        User repartidor = new User(2, "repartidor", "rep@example.com", "pass", Role.DELIVERY);

        Order order1 = createTestOrder(1, OrderStatus.PENDIENTE, BigDecimal.valueOf(50.0));
        Order order2 = createTestOrder(2, OrderStatus.PROCESANDO, BigDecimal.valueOf(75.0));
        Order order3 = createTestOrder(3, OrderStatus.ENTREGADO, BigDecimal.valueOf(100.0));

        List<Order> orders = Arrays.asList(order1, order2, order3);
        when(orderRepository.findByRepartidor(repartidor)).thenReturn(orders);

        // When
        Map<String, Object> stats = orderService.getDeliveryStats(repartidor);

        // Then
        assertThat(stats).isNotNull();
        assertThat(stats.get("pedidosPendientes")).isEqualTo(1L);
        assertThat(stats.get("pedidosEnProceso")).isEqualTo(1L);
        assertThat(stats.get("pedidosEntregados")).isEqualTo(1L);
        assertThat(stats.get("totalGanancias")).isEqualTo(100.0);
    }

    @Test
    void assignRepartidor_ValidAssignment_AssignsSuccessfully() {
        // Given
        User repartidor = new User(2, "repartidor", "rep@example.com", "pass", Role.DELIVERY);
        when(orderRepository.findById(1)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // When
        Order result = orderService.assignRepartidor(1, repartidor);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRepartidor()).isEqualTo(repartidor);
        verify(orderRepository).save(testOrder);
    }

    @Test
    void getAllOrders_ReturnsAllOrders() {
        // Given
        List<Order> expectedOrders = Arrays.asList(testOrder);
        when(orderRepository.findAll()).thenReturn(expectedOrders);

        // When
        List<Order> result = orderService.getAllOrders();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(orderRepository).findAll();
    }

    @Test
    void countOrdersByStatus_ReturnsCorrectCount() {
        // Given
        when(orderRepository.countByStatus(OrderStatus.PENDIENTE)).thenReturn(5L);

        // When
        long count = orderService.countOrdersByStatus(OrderStatus.PENDIENTE);

        // Then
        assertThat(count).isEqualTo(5);
        verify(orderRepository).countByStatus(OrderStatus.PENDIENTE);
    }

    private Order createTestOrder(Integer id, OrderStatus status, BigDecimal total) {
        Order order = new Order();
        order.setId(id);
        order.setStatus(status);
        order.setTotalAmount(total);
        order.setCreatedAt(LocalDateTime.now());
        return order;
    }
}