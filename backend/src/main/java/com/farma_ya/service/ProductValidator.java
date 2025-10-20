package com.farma_ya.service;

import com.farma_ya.model.Product;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Validador de productos que cumple con SRP y OCP
 * - SRP: Solo se encarga de validar productos
 * - OCP: Extensible para nuevas reglas de validación sin modificar código
 * existente
 */
@Component
public class ProductValidator {

    /**
     * Valida un producto según las reglas de negocio
     */
    public void validateProduct(Product product) {
        validatePrice(product.getPrice());
        validateStock(product.getStock());
        validateRequiredFields(product);
    }

    /**
     * Valida que el precio sea positivo
     */
    private void validatePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio debe ser un valor positivo");
        }
    }

    /**
     * Valida que el stock no sea negativo
     */
    private void validateStock(Integer stock) {
        if (stock == null || stock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
    }

    /**
     * Valida que los campos requeridos estén presentes
     */
    private void validateRequiredFields(Product product) {
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del producto es requerido");
        }
        if (product.getPrice() == null) {
            throw new IllegalArgumentException("El precio del producto es requerido");
        }
    }

    /**
     * Validación específica para actualización de productos
     */
    public void validateForUpdate(Product existingProduct, Product updatedProduct) {
        validateProduct(updatedProduct);

        // Reglas específicas para actualización pueden ir aquí
        // Ejemplo: no permitir cambiar SKU si ya tiene ventas
        if (existingProduct.getSku() != null &&
                !existingProduct.getSku().equals(updatedProduct.getSku())) {
            // Esta validación podría verificar si el producto tiene ventas
            // throw new IllegalArgumentException("No se puede cambiar el SKU de un producto
            // con ventas");
        }
    }
}