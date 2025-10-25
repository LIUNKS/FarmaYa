package com.farma_ya.service;

import com.farma_ya.model.Cart;
import com.farma_ya.model.CartItem;
import com.farma_ya.model.Product;
import com.farma_ya.model.User;
import com.farma_ya.repository.CartRepository;
import com.farma_ya.repository.ProductRepository;
import com.farma_ya.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CartService implements ICartService {

    private final CartRepository cartRepository;

    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    public Cart getCartByUser(User user) {
        return cartRepository.findByUserWithItemsAndProducts(user)
                .orElseGet(() -> createCartForUser(user));
    }

    public Cart addToCart(User user, Long productId, int quantity) {
        Cart cart = getCartByUser(user);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        int newQuantity = quantity;
        if (existingItem.isPresent()) {
            newQuantity += existingItem.get().getQuantity();
        }

        if (product.getStock() < newQuantity) {
            throw new IllegalArgumentException(
                    "Stock insuficiente para " + product.getName() + ". Disponible: " + product.getStock());
        }

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(newQuantity);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            cart.getItems().add(newItem);
        }

        return cartRepository.save(cart);
    }

    public Cart removeFromCart(User user, Long productId) {
        Cart cart = getCartByUser(user);
        cart.getItems().removeIf(item -> item.getProduct().getId().equals(productId));
        return cartRepository.save(cart);
    }

    public Cart clearCart(User user) {
        Cart cart = getCartByUser(user);
        cart.getItems().clear();
        return cartRepository.save(cart);
    }

    private Cart createCartForUser(User user) {
        Cart cart = new Cart();
        cart.setUser(user);
        return cartRepository.save(cart);
    }
}