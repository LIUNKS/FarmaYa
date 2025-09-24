
package com.farma_ya.service;

import com.farma_ya.model.Category;

import com.farma_ya.model.Product;
import com.farma_ya.repository.ProductRepository;
import com.farma_ya.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));
    }

    public Product createProduct(Product product) {
        if (product.getPrice() <= 0 || product.getStock() < 0) {
            throw new IllegalArgumentException("Precio y stock deben ser valores válidos");
        }
        return productRepository.save(product);
    }

    public Product updateProduct(Long id, Product productDetails) {
        Product product = getProductById(id);
        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setStock(productDetails.getStock());
        product.setImageUrl(productDetails.getImageUrl());
        product.setCategory(productDetails.getCategory());
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
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
        try {
            Category enumCategory = Category.valueOf(category.toUpperCase());
            return productRepository.findByCategory(enumCategory.name());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Categoría inválida: " + category);
        }
    }

    @PostConstruct
    public void initData() {
        if (productRepository.count() == 0) {
            Product p1 = new Product();
            p1.setName("Paracetamol 500mg");
            p1.setDescription("Analgésico para dolor");
            p1.setPrice(5.99);
            p1.setStock(100);
            p1.setImageUrl("https://via.placeholder.com/150?text=Paracetamol");
            p1.setCategory(Category.MEDICAMENTO);

            Product p2 = new Product();
            p2.setName("Crema Hidratante");
            p2.setDescription("Hidratante para piel seca");
            p2.setPrice(12.50);
            p2.setStock(50);
            p2.setImageUrl("https://via.placeholder.com/150?text=Crema");
            p2.setCategory(Category.COSMETICO);

            Product p3 = new Product();
            p3.setName("Jabón Antibacterial");
            p3.setDescription("Jabón para higiene diaria");
            p3.setPrice(3.20);
            p3.setStock(200);
            p3.setImageUrl("https://via.placeholder.com/150?text=Jabon");
            p3.setCategory(Category.HIGIENE);

            Product p4 = new Product();
            p4.setName("Vitamina C 1000mg");
            p4.setDescription("Suplemento vitamínico");
            p4.setPrice(8.75);
            p4.setStock(75);
            p4.setImageUrl("https://via.placeholder.com/150?text=Vitamina");
            p4.setCategory(Category.SUPLEMENTO);

            productRepository.saveAll(List.of(p1, p2, p3, p4));
        }
    }
}