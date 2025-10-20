package com.farma_ya.service;

import com.farma_ya.model.Order;
import com.farma_ya.model.User;
import java.util.List;

/**
 * Interface para el servicio de gestión de pedidos
 * Cumple con ISP: solo expone métodos necesarios para los clientes
 */
public interface IOrderService {

    Order createOrderFromCart(User user);

    List<Order> getOrdersByUser(User user);

    Order getOrderById(Long id);

    Order updateOrderStatus(Long id, String status);
}