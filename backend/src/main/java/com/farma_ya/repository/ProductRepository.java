package com.farma_ya.repository;

import com.farma_ya.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByCategoriaContainingIgnoreCase(String categoria);

    List<Product> findByStockGreaterThanEqual(int stock);

    List<Product> findByStockLessThanEqual(int stock);
}