package com.farma_ya.service;

import com.farma_ya.model.Product;
import java.util.List;

/**
 * Interface para el servicio de gestión de productos
 * Cumple con ISP: solo expone métodos necesarios para los clientes
 */
public interface IProductService {

    List<Product> getAllProducts();

    Product getProductById(Long id);

    Product createProduct(Product product);

    Product updateProduct(Long id, Product productDetails);

    void deleteProduct(Long id);

    List<Product> searchProducts(String name);

    List<Product> getProductsByCategory(String category);
}