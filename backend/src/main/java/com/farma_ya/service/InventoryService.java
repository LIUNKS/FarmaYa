package com.farma_ya.service;

import com.farma_ya.model.Product;
import com.farma_ya.repository.ProductRepository;
import com.farma_ya.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InventoryService {

    private final ProductRepository productRepository;

    public InventoryService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Verifica si hay suficiente stock para un producto
     */
    public boolean hasEnoughStock(Integer productId, int requiredQuantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + productId));
        return product.getStock() >= requiredQuantity;
    }

    /**
     * Reduce el stock de un producto de forma segura
     */
    public void decrementStock(Integer productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + productId));

        if (product.getStock() < quantity) {
            throw new IllegalArgumentException("Stock insuficiente para " + product.getName() +
                    ". Disponible: " + product.getStock() + ", Requerido: " + quantity);
        }

        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
    }

    /**
     * Verifica y reduce stock en una operación atómica
     */
    public void checkAndDecrementStock(Integer productId, int quantity) {
        if (!hasEnoughStock(productId, quantity)) {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + productId));
            throw new IllegalArgumentException("Stock insuficiente para " + product.getName());
        }
        decrementStock(productId, quantity);
    }
}