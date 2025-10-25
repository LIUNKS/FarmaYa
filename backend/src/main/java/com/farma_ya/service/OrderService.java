
package com.farma_ya.service;

import com.farma_ya.model.Direccion;
import com.farma_ya.model.OrderStatus;
import com.farma_ya.model.Cart;
import com.farma_ya.model.CartItem;
import com.farma_ya.model.Order;
import com.farma_ya.model.OrderItem;
import com.farma_ya.model.User;
import com.farma_ya.repository.DireccionRepository;
import com.farma_ya.repository.OrderRepository;
import com.farma_ya.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderService implements IOrderService {

    private final OrderRepository orderRepository;

    private final DireccionRepository direccionRepository;

    private final ICartService cartService;

    private final InventoryService inventoryService;

    public OrderService(OrderRepository orderRepository, DireccionRepository direccionRepository,
            ICartService cartService, InventoryService inventoryService) {
        this.orderRepository = orderRepository;
        this.direccionRepository = direccionRepository;
        this.cartService = cartService;
        this.inventoryService = inventoryService;
    }

    @Transactional
    public Order createOrderFromCart(User user, Map<String, String> shippingData) {
        Cart cart = cartService.getCartByUser(user);
        if (cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("El carrito está vacío");
        }

        // Verificar y actualizar stock usando InventoryService
        for (CartItem item : cart.getItems()) {
            inventoryService.checkAndDecrementStock(item.getProduct().getId(), item.getQuantity());
        }

        Order order = new Order();
        order.setUser(user);
        order.setNumeroPedido(generateOrderNumber());

        // Crear dirección de entrega y guardarla primero
        if (shippingData != null && !shippingData.isEmpty()) {
            Direccion direccion = new Direccion();
            direccion.setUser(user);
            direccion.setDireccionLinea(shippingData.get("shippingAddress"));
            direccion.setDistrito(shippingData.get("shippingDistrict"));
            direccion.setReferencia(shippingData.get("shippingReference"));
            // Guardar la dirección primero
            Direccion savedDireccion = direccionRepository.save(direccion);
            order.setShippingAddress(savedDireccion);
        }

        List<OrderItem> orderItems = cart.getItems().stream().map(this::convertToOrderItem)
                .collect(Collectors.toList());
        order.setItems(orderItems);

        // Calcular el total basado en los items
        BigDecimal calculatedTotal = orderItems.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(calculatedTotal);

        // Establecer relación bidireccional
        for (OrderItem orderItem : orderItems) {
            orderItem.setOrder(order);
        }

        // Guardar la orden primero
        Order savedOrder = orderRepository.save(order);

        // Limpiar el carrito después de guardar la orden exitosamente
        cartService.clearCart(user);

        return savedOrder;
    }

    private OrderItem convertToOrderItem(CartItem cartItem) {
        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(cartItem.getProduct());
        orderItem.setQuantity(cartItem.getQuantity());
        orderItem.setPrice(cartItem.getProduct().getPrice());

        // Calcular subtotal
        BigDecimal subtotal = cartItem.getProduct().getPrice()
                .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
        orderItem.setSubtotal(subtotal);

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

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    private String generateOrderNumber() {
        // Generar número de pedido único: PED + timestamp + random
        return "PED" + System.currentTimeMillis() + (int) (Math.random() * 1000);
    }
}