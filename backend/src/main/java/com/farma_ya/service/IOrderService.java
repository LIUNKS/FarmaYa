package com.farma_ya.service;

import com.farma_ya.model.Order;
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

    Order getOrderById(Long id);

    Order updateOrderStatus(Long id, String status);

    List<Order> getAllOrders();
}