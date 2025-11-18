package com.farma_ya.service;

import com.farma_ya.model.Cart;
import com.farma_ya.model.User;

/**
 * Interface para el servicio de gestión de carrito
 * Cumple con ISP: solo expone métodos necesarios para los clientes
 */
public interface ICartService {

    Cart getCartByUser(User user);

    Cart addToCart(User user, Integer productId, int quantity);

    Cart removeFromCart(User user, Integer productId);

    Cart clearCart(User user);
}