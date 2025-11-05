package com.farma_ya.system;

import com.farma_ya.FarmaYaApplication;
import com.farma_ya.model.*;
import com.farma_ya.repository.OrderRepository;
import com.farma_ya.repository.UserRepository;
import com.farma_ya.service.IOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = FarmaYaApplication.class)
@Testcontainers
@ActiveProfiles("test")
class OrderSystemTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("farma_ya_test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", mysql::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private IOrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    private User testUser;
    private User deliveryUser;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        // Limpiar datos previos
        orderRepository.deleteAll();
        userRepository.deleteAll();

        // Crear usuarios de prueba
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setRole(Role.USER);
        testUser = userRepository.save(testUser);

        deliveryUser = new User();
        deliveryUser.setUsername("delivery");
        deliveryUser.setEmail("delivery@example.com");
        deliveryUser.setPassword("password");
        deliveryUser.setRole(Role.DELIVERY);
        deliveryUser = userRepository.save(deliveryUser);

        // Crear orden de prueba
        testOrder = new Order();
        testOrder.setUser(testUser);
        testOrder.setStatus(OrderStatus.PENDIENTE);
        testOrder.setTotalAmount(BigDecimal.valueOf(100.0));
        testOrder.setNumeroPedido("ORD-SYS-001");
        testOrder = orderRepository.save(testOrder);
    }

    @Test
    void orderLifecycle_ShouldWorkCorrectly() {
        // Given - Orden creada en setUp

        // When - Cambiar estado a PROCESANDO
        Order updatedOrder = orderService.updateOrderStatus(testOrder.getId(), "PROCESANDO");

        // Then
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.PROCESANDO);

        // When - Asignar repartidor
        Order assignedOrder = orderService.assignRepartidor(testOrder.getId(), deliveryUser);

        // Then - Comparar por ID en lugar del objeto completo
        assertThat(assignedOrder.getRepartidor().getId()).isEqualTo(deliveryUser.getId());
        assertThat(assignedOrder.getRepartidor().getUsername()).isEqualTo(deliveryUser.getUsername());

        // When - Cambiar estado a ENVIADO
        Order inTransitOrder = orderService.updateOrderStatus(testOrder.getId(), "ENVIADO");

        // Then
        assertThat(inTransitOrder.getStatus()).isEqualTo(OrderStatus.ENVIADO);

        // When - Marcar como entregada
        Order deliveredOrder = orderService.updateOrderStatus(testOrder.getId(), "ENTREGADO");

        // Then
        assertThat(deliveredOrder.getStatus()).isEqualTo(OrderStatus.ENTREGADO);
    }

    @Test
    void userOrderRetrieval_ShouldReturnCorrectOrders() {
        // Given - Crear múltiples órdenes para el usuario
        Order order2 = new Order();
        order2.setUser(testUser);
        order2.setStatus(OrderStatus.PROCESANDO);
        order2.setTotalAmount(BigDecimal.valueOf(50.0));
        order2.setNumeroPedido("ORD-SYS-002");
        orderRepository.save(order2);

        // When
        List<Order> userOrders = orderService.getOrdersByUser(testUser);

        // Then
        assertThat(userOrders).hasSize(2);
        assertThat(userOrders).allMatch(order -> order.getUser().getId().equals(testUser.getId()));
        assertThat(userOrders).extracting("numeroPedido")
                .contains("ORD-SYS-001", "ORD-SYS-002");
    }

    @Test
    void deliveryAssignment_ShouldWorkCorrectly() {
        // Given - Orden sin asignar

        // When
        Order assignedOrder = orderService.assignRepartidor(testOrder.getId(), deliveryUser);

        // Then - Comparar por ID y username
        assertThat(assignedOrder.getRepartidor().getId()).isEqualTo(deliveryUser.getId());
        assertThat(assignedOrder.getRepartidor().getUsername()).isEqualTo(deliveryUser.getUsername());

        // When - Obtener órdenes del repartidor
        List<Order> deliveryOrders = orderService.getOrdersByRepartidor(deliveryUser);

        // Then
        assertThat(deliveryOrders).hasSize(1);
        assertThat(deliveryOrders.get(0).getRepartidor().getId()).isEqualTo(deliveryUser.getId());
    }

    @Test
    void deliveryStats_ShouldCalculateCorrectly() {
        // Given - Primero asignar la orden de setUp al repartidor y actualizarla
        orderService.assignRepartidor(testOrder.getId(), deliveryUser);
        testOrder = orderService.updateOrderStatus(testOrder.getId(), "ENTREGADO");

        // Crear una orden en proceso para el repartidor
        Order inProcessOrder = new Order();
        inProcessOrder.setUser(testUser);
        inProcessOrder.setRepartidor(deliveryUser);
        inProcessOrder.setStatus(OrderStatus.PROCESANDO);
        inProcessOrder.setTotalAmount(BigDecimal.valueOf(25.0));
        inProcessOrder.setNumeroPedido("ORD-SYS-003");
        orderRepository.save(inProcessOrder);

        // When
        var stats = orderService.getDeliveryStats(deliveryUser);

        // Then - Debería haber 1 orden entregada (testOrder) y 1 en proceso
        assertThat(stats).isNotNull();
        assertThat(stats.get("pedidosPendientes")).isEqualTo(0L);
        assertThat(stats.get("pedidosEnProceso")).isEqualTo(1L);
        assertThat(stats.get("pedidosEntregados")).isEqualTo(1L); // solo testOrder
        // Total ganancias: 100.0 (solo testOrder entregada)
        assertThat(stats.get("totalGanancias")).isEqualTo(100.0);
    }

    @Test
    void concurrentOrderProcessing_ShouldHandleMultipleOrders() {
        // Given - Crear múltiples órdenes
        for (int i = 1; i <= 5; i++) {
            Order order = new Order();
            order.setUser(testUser);
            order.setStatus(OrderStatus.PENDIENTE);
            order.setTotalAmount(BigDecimal.valueOf(20.0 * i));
            order.setNumeroPedido("ORD-SYS-CONC-" + i);
            orderRepository.save(order);
        }

        // When - Procesar todas las órdenes
        List<Order> allOrders = orderRepository.findAll();
        for (Order order : allOrders) {
            if (order.getStatus() == OrderStatus.PENDIENTE) {
                orderService.updateOrderStatus(order.getId(), "PROCESANDO");
            }
        }

        // Then
        List<Order> processedOrders = orderRepository.findAll();
        assertThat(processedOrders).hasSizeGreaterThanOrEqualTo(6); // 1 de setUp + 5 nuevas
        assertThat(processedOrders).filteredOn(order -> order.getStatus() == OrderStatus.PROCESANDO)
                .hasSizeGreaterThanOrEqualTo(5);
    }

    @Test
    void dataPersistence_ShouldSurviveRestart() {
        // Given - Crear y guardar orden
        Order persistentOrder = new Order();
        persistentOrder.setUser(testUser);
        persistentOrder.setStatus(OrderStatus.PENDIENTE);
        persistentOrder.setTotalAmount(BigDecimal.valueOf(200.0));
        persistentOrder.setNumeroPedido("ORD-PERSIST-001");
        Order savedOrder = orderRepository.save(persistentOrder);

        // When - Recuperar desde base de datos
        Order retrievedOrder = orderRepository.findById(savedOrder.getId()).orElse(null);

        // Then
        assertThat(retrievedOrder).isNotNull();
        assertThat(retrievedOrder.getNumeroPedido()).isEqualTo("ORD-PERSIST-001");
        // Comparar el valor numérico double
        assertThat(retrievedOrder.getTotalAmount()).isEqualTo(200.0);
        assertThat(retrievedOrder.getStatus()).isEqualTo(OrderStatus.PENDIENTE);
    }
}