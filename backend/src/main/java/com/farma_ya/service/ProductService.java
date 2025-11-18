
package com.farma_ya.service;

import com.farma_ya.model.Product;
import com.farma_ya.repository.ProductRepository;
import com.farma_ya.exception.ResourceNotFoundException;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService implements IProductService {

    private final ProductRepository productRepository;
    private final ProductValidator productValidator;

    public ProductService(ProductRepository productRepository, ProductValidator productValidator) {
        this.productRepository = productRepository;
        this.productValidator = productValidator;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Integer id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));
    }

    public Product createProduct(Product product) {
        productValidator.validateProduct(product);
        return productRepository.save(product);
    }

    public Product updateProduct(Integer id, Product productDetails) {
        Product existingProduct = getProductById(id);

        // Actualizar campos
        existingProduct.setName(productDetails.getName());
        existingProduct.setDescription(productDetails.getDescription());
        existingProduct.setPrice(productDetails.getPrice());
        existingProduct.setStock(productDetails.getStock());
        existingProduct.setImageUrl(productDetails.getImageUrl());
        existingProduct.setCategoria(productDetails.getCategoria());

        // Validar producto actualizado
        productValidator.validateForUpdate(getProductById(id), existingProduct);

        return productRepository.save(existingProduct);
    }

    public void deleteProduct(Integer id) {
        Product product = getProductById(id);
        productRepository.delete(product);
    }

    public List<Product> searchProducts(String name) {
        if (name == null || name.trim().isEmpty()) {
            return getAllProducts();
        }
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategoriaContainingIgnoreCase(category);
    }

}