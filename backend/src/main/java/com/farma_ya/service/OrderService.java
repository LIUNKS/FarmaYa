
package com.farma_ya.service;

import com.farma_ya.model.Product;
import com.farma_ya.model.OrderStatus;

import com.farma_ya.model.Cart;
import com.farma_ya.model.CartItem;
import com.farma_ya.model.Order;
import com.farma_ya.model.OrderItem;
import com.farma_ya.model.User;
import com.farma_ya.repository.OrderRepository;
import com.farma_ya.repository.ProductRepository;
import com.farma_ya.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public Order createOrderFromCart(User user) {
        Cart cart = cartService.getCartByUser(user);
        if (cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("El carrito está vacío");
        }

        // Verificar y actualizar stock
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            if (product.getStock() < item.getQuantity()) {
                throw new IllegalArgumentException("Stock insuficiente para " + product.getName());
            }
            product.setStock(product.getStock() - item.getQuantity());
            productRepository.save(product);
        }

        Order order = new Order();
        order.setUser(user);
        order.setTotalAmount(cart.getTotalAmount());
        List<OrderItem> orderItems = cart.getItems().stream().map(this::convertToOrderItem)
                .collect(Collectors.toList());
        order.setItems(orderItems);

        // Establecer relación bidireccional
        for (OrderItem orderItem : orderItems) {
            orderItem.setOrder(order);
        }

        cartService.clearCart(user);

        return orderRepository.save(order);
    }

    private OrderItem convertToOrderItem(CartItem cartItem) {
        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(cartItem.getProduct());
        orderItem.setQuantity(cartItem.getQuantity());
        orderItem.setPrice(cartItem.getProduct().getPrice());
        return orderItem;
    }

    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findByUser(user);
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada con ID: " + id));
    }

    public Order updateOrderStatus(Long id, String status) {
        Order order = getOrderById(id);
        try {
            order.setStatus(OrderStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Estado de orden inválido: " + status);
        }
        return orderRepository.save(order);
    }
}