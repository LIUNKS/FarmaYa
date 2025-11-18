package com.farma_ya.service;

import com.farma_ya.model.Order;
import com.farma_ya.model.OrderStatus;
import com.farma_ya.model.User;
import java.util.List;
import java.util.Map;

/**
 * Interface para el servicio de gestión de pedidos
 * Cumple con ISP: solo expone métodos necesarios para los clientes
 */
public interface IOrderService {

    Order createOrderFromCart(User user, Map<String, String> shippingData);

    List<Order> getOrdersByUser(User user);

    Order getOrderById(Integer id);

    Order updateOrderStatus(Integer id, String status);

    List<Order> getAllOrders();

    long countOrdersByStatus(OrderStatus status);

    List<Order> getOrdersByRepartidor(User repartidor);

    List<Order> getRecentOrders(int limit);

    Order assignRepartidor(Integer orderId, User repartidor);

    List<Order> getUnassignedOrdersByStatus(OrderStatus status);

    Map<String, Object> getDeliveryStats(User repartidor);
}